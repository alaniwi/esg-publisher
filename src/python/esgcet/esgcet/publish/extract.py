import os
import logging
import numpy
import re
import datetime
from esgcet.messaging import debug, info, warning, error, critical, exception
from esgcet.model import *
from esgcet.exceptions import *
from esgcet.config import splitLine, getConfig
from utility import getTypeAndLen, issueCallback, compareFiles, checksum, extraFieldsGet

NAME=0
LENGTH=1
SEQ=2

CREATE_OP=1
DELETE_OP=2
RENAME_OP=3
UPDATE_OP=4
REPLACE_OP=5

# When translating numpy arrays (e.g., dimension values) to strings, don't include newlines
numpy.set_printoptions(threshold=numpy.inf, linewidth=numpy.inf)

def extractFromDataset(datasetName, fileIterator, dbSession, handler, cfHandler, aggregateDimensionName=None, offline=False, operation=CREATE_OP, progressCallback=None, stopEvent=None, keepVersion=False, newVersion=None, extraFields=None, masterGateway=None, comment=None, useVersion=-1, forceRescan=False, **context):
    """
    Extract metadata from a dataset represented by a list of files, add to a database. Populates the database tables:

    - dataset
    - dataset_version
    - file
    - file_version
    - dataset_file_version
    - file_variable (partially)
    - associated attribute tables

    Returns a Dataset object.

    datasetName
      String dataset identifier.

    fileIterator
      An iterator that returns an iteration of (file_path, file_size), where file_size is an integer.

    dbSession
      A database Session.

    handler
      Project handler

    cfHandler  
      A CF handler instance

    aggregateDimensionName
      The name of the dimension across which the dataset is aggregated, if any.

    offline
      Boolean, True if the files are offline, cannot be scanned.

    operation
      Publication operation, one of CREATE_OP, DELETE_OP, RENAME_OP, UPDATE_OP

    progressCallback
      Tuple (callback, initial, final) where ``callback`` is a function of the form ``callback(progress)``, ``initial`` is the initial value reported, ``final`` is the final value reported.

    stopEvent
      Object with boolean attribute ``stop_extract`` (for example, ``utility.StopEvent``). If set to True (in another thread) the extraction is stopped.

    keepVersion
      Boolean, True if the dataset version should not be incremented.

    newVersion
      Set the new version number explicitly. By default the version number is incremented by 1. See keepVersion.

    extraFields
      Extra fields dictionary, as from ``readDatasetMap``.

    masterGateway
      The gateway that owns the master copy of the datasets. If None, the dataset is not replicated.
      Otherwise the TDS catalog is written with a 'master_gateway' property, flagging the dataset(s)
      as replicated.

    comment
      String comment on the dataset version. If the dataset version is not increased, the comment is ignored.

    useVersion=-1:
      Integer version number of the dataset version to modify. By default the latest version is modified.

    forceRescan
      Boolean, if True force all files to be rescanned on an update.

    context
      A dictionary with keys ``project``, ``model``, ``experiment``, etc. The context consists of all fields needed to uniquely define the dataset.

    """

    session = dbSession()

    # Get configuration options related to the scan
    configOptions = {}
    config = getConfig()
    if config is not None:
        section = 'project:%s'%context.get('project')
        vlstring = config.get(section, 'variable_locate', default=None)
        if vlstring is not None:
            fields = splitLine(vlstring)
            varlocate = [s.split(',') for s in fields]
        else:
            varlocate = None

        line = config.get('DEFAULT', 'checksum', default=None)
        if line is not None:
            checksumClient, checksumType = splitLine(line)
        else:
            checksumClient = None
            checksumType = None
    else:
        varlocate = None
        checksumClient = None
        checksumType = None
        
    configOptions['variable_locate'] = varlocate
    configOptions['checksumClient'] = checksumClient
    configOptions['checksumType'] = checksumType

    # Check if the dataset / version is already in the database
    dset = session.query(Dataset).filter_by(name=datasetName).first()
    if dset is not None:
        if operation==CREATE_OP:
            operation = REPLACE_OP
    else:
        if operation in [UPDATE_OP, REPLACE_OP]:
            operation = CREATE_OP
        elif operation in [DELETE_OP, RENAME_OP]:
            raise ESGPublishError("No such dataset: %s"%datasetName)

    # Cannot add online files to offline dataset, and vice versa
    if dset is not None and dset.offline != offline:
        if dset.offline:
            raise ESGPublishError("Dataset %s is offline, set offline flag or replace the dataset."%dset.name)
        else:
            raise ESGPublishError("Dataset %s is online, but offline flag is set."%dset.name)

    # Cannot publish a replica with the same ID as a local dataset and vice versa
    if dset is not None and dset.master_gateway != masterGateway:
        if dset.master_gateway is None:
            raise ESGPublishError("Dataset %s exists and is not a replica - delete it before publishing a replica of the same name."%dset.name)
        else:
            raise ESGPublishError("Dataset %s exists and is a replica. Use --replica or delete the existing dataset."%dset.name)

    createTime = datetime.datetime.now() # DatasetVersion creation_time
    fobjs = None
    pathlist = [item for item in fileIterator]
    if operation==CREATE_OP:
        # Create a new dataset
        info("Creating dataset: %s"%datasetName)
        dset = Dataset(datasetName, context.get('project', None), context.get('model', None), context.get('experiment', None), context.get('run_name', None), offline=offline, masterGateway=masterGateway)
        session.add(dset)

        # Create an initial dataset version
        existingVersion = 0
        eventFlag = CREATE_DATASET_EVENT
        addNewVersion, fobjs = createDataset(dset, pathlist, session, handler, cfHandler, configOptions, aggregateDimensionName=aggregateDimensionName, offline=offline, progressCallback=progressCallback, stopEvent=stopEvent, extraFields=extraFields, masterGateway=masterGateway, **context)
        
    elif operation in [UPDATE_OP, REPLACE_OP]:
        versionObj = dset.getVersionObj(useVersion)
        if versionObj is None:
            raise ESGPublishError("Version %d of dataset %s not found, cannot republish."%(useVersion, dset.name))
        existingVersion = dset.getVersion()
        eventFlag = UPDATE_DATASET_EVENT
        addNewVersion, fobjs = updateDatasetVersion(dset, versionObj, pathlist, session, handler, cfHandler, configOptions, aggregateDimensionName=aggregateDimensionName, offline=offline, progressCallback=progressCallback, stopEvent=stopEvent, extraFields=extraFields, replace=(operation==REPLACE_OP), forceRescan=forceRescan, **context)
         
    elif operation==RENAME_OP:
        versionObj = dset.getVersionObj(useVersion)
        if versionObj is None:
            raise ESGPublishError("Version %d of dataset %s not found, cannot republish."%(useVersion, dset.name))
        existingVersion = dset.getVersion()
        eventFlag = UPDATE_DATASET_EVENT
        addNewVersion = renameFilesVersion(dset, versionObj, pathlist, session, cfHandler, configOptions, aggregateDimensionName=aggregateDimensionName, offline=offline, progressCallback=progressCallback, stopEvent=stopEvent, extraFields=extraFields, **context)
         
    elif operation==DELETE_OP:
        versionObj = dset.getVersionObj(useVersion)
        if versionObj is None:
            raise ESGPublishError("Version %d of dataset %s not found, cannot republish."%(useVersion, dset.name))
        existingVersion = dset.getVersion()
        eventFlag = UPDATE_DATASET_EVENT
        addNewVersion, fobjs = deleteFilesVersion(dset, versionObj, pathlist, session, cfHandler, configOptions, aggregateDimensionName=aggregateDimensionName, offline=offline, progressCallback=progressCallback, stopEvent=stopEvent, extraFields=extraFields, **context)
    else:
        raise ESGPublishError("Invalid dataset operation: %s"%`operation`)

    # Create a new dataset version if necessary
    if keepVersion:
        newVersion = existingVersion
    elif newVersion is None:
        newVersion = existingVersion + 1
        
    dset.reaggregate = False
    if addNewVersion and newVersion>existingVersion:
        newDsetVersionObj = DatasetVersionFactory(dset, version=newVersion, creation_time=createTime, comment=comment)
        info("New dataset version = %d"%newDsetVersionObj.version)
        for var in dset.variables:
            session.delete(var)
        newDsetVersionObj.files.extend(fobjs)
        event = Event(datasetName, newDsetVersionObj.version, eventFlag)
        dset.events.append(event)
        dset.reaggregate = True
    elif masterGateway is not None:     # Force version set on replication
        info("Dataset version = %d"%newVersion)
        dset.setVersion(newVersion)
        event = Event(datasetName, newVersion, eventFlag)
        dset.events.append(event)

    info("Adding file info to database")
    session.commit()
    session.close()

    return dset

def createDataset(dset, pathlist, session, handler, cfHandler, configOptions, aggregateDimensionName=None, offline=False, progressCallback=None, stopEvent=None, extraFields=None, masterGateway=None, **context):

    fobjlist = []                       # File objects in the dataset
    nfiles = len(pathlist)

    basedict = {}                       # file.base => 1
    varlocate = configOptions['variable_locate']
    checksumClient = configOptions['checksumClient']
    checksumType = configOptions['checksumType']
    seq = 0
    for path, sizet in pathlist:
        size, mtime = sizet

        csum = None
        csumtype = checksumType
        if extraFields is not None:
            csum = extraFields.get((dset.name, -1, path, 'checksum'), None)
            csumtype = extraFields.get((dset.name, -1, path, 'checksum_type'), None)
        if csum is None and not offline and checksumClient is not None:
            csum = checksum(path, checksumClient)
            csumtype = checksumType

        # Create a file and version
        base = generateFileBase(path, basedict, dset.name)
        file = File(base, 'netCDF')
        basedict[base] = 1
        fileVersion = FileVersion(1, path, size, mod_time=mtime, checksum=csum, checksum_type=csumtype)
        file.versions.append(fileVersion)
        fobjlist.append(fileVersion)
        seq += 1

        dset.files.append(file)

        # Extract the dataset contents
        if not offline:
            info("Scanning %s"%path)
            f = handler.openPath(path)
            extractFromFile(dset, f, file, session, cfHandler, aggdimName=aggregateDimensionName, varlocate=varlocate, **context)
            f.close()
        else:
            info("File %s is offline"%path)

        # Callback progress
        try:
            issueCallback(progressCallback, seq, nfiles, 0, 1, stopEvent=stopEvent)
        except:
            session.rollback()
            session.close()
            raise

    return True, fobjlist

def updateDatasetVersion(dset, dsetVersion, pathlist, session, handler, cfHandler, configOptions, aggregateDimensionName=None, offline=False, progressCallback=None, stopEvent=None, extraFields=None, replace=False, forceRescan=False, **context):

    if replace:
        info("Replacing files in dataset: %s, version %d"%(dset.name, dsetVersion.version))
    else:
        info("Updating files in dataset: %s, version %d"%(dset.name, dsetVersion.version))

    haveLatestDsetVersion = (dsetVersion.version == dset.getVersion())

    # Get the list of FileVersion objects for this version
    locdict = {}
    todelete = {}
    for fobj in dsetVersion.getFileVersions():
        loc = fobj.location
        locdict[loc] = todelete[loc] = fobj

    varlocate = configOptions['variable_locate']
    checksumClient = configOptions['checksumClient']
    checksumType = configOptions['checksumType']

    # Get the base dictionary for the entire dataset
    basedict = dset.getBaseDictionary()

    # For each item in the pathlist:
    seq = 0
    fileModified = False                # Any file has been modified (added, replaced, or deleted)
    newFileVersionObjs = []
    nfiles = len(pathlist)
    for path, sizet in pathlist:

        # Rescan this file if it has been added, or replaced
        rescanFile = haveLatestDsetVersion

        size, mtime=sizet
        csum = None
        csumtype = checksumType
        if extraFields is not None:
            csum = extraFieldsGet(extraFields, (dset.name, path, 'checksum'), dsetVersion)
            csumtype = extraFieldsGet(extraFields, (dset.name, path, 'checksum_type'), dsetVersion)
        if csum is None and not offline and checksumClient is not None:
            csum = checksum(path, checksumClient)
            csumtype = checksumType

        # If the item is in the current dataset version, get the file version obj and add to the list
        if locdict.has_key(path):
            del todelete[path]
            fileVersionObj = locdict[path]
            fileObj = fileVersionObj.parent
            
            # If the file matches the existing file version, no-op, ...
            if compareFiles(fileVersionObj, handler, path, size, offline, checksum=csum):
                if not forceRescan:
                    info("File %s exists, skipping"%path)
                newFileVersionObjs.append(fileVersionObj)
                rescanFile = False

            # ... else create a new version of the file
            else:
                newFileVersionObj = FileVersionFactory(fileObj, path, session, size, mod_time=mtime, checksum=csum, checksum_type=csumtype)
                newFileVersionObjs.append(newFileVersionObj)
                fileObj.deleteChildren(session)
                fileModified = True

        # Else create a new file / file version object and add to the list ...
        else:
            fileObj = FileFactory(dset, path, basedict, session)
            newFileVersionObj = FileVersionFactory(fileObj, path, session, size, mod_time=mtime, checksum=csum, checksum_type=csumtype)
            newFileVersionObjs.append(newFileVersionObj)
            fileModified = True

        # ... and rescan if necessary
        if rescanFile or forceRescan:
            if not offline:
                info("Scanning %s"%path)
                f = handler.openPath(path)
                extractFromFile(dset, f, fileObj, session, cfHandler, aggdimName=aggregateDimensionName, varlocate=varlocate, **context)
                f.close()
            else:
                info("File %s is offline"%path)

        # Callback progress
        seq += 1
        try:
            issueCallback(progressCallback, seq, nfiles, 0, 1, stopEvent=stopEvent)
        except:
            session.rollback()
            session.close()
            raise

    # If updating, add the file version objects ...
    if not replace:
        for fileVersionObj in todelete.values():
            newFileVersionObjs.append(fileVersionObj)

    # ... else if rescanning delete the file object children
    elif haveLatestDsetVersion:
        for fileVersionObj in todelete.values():
            fileObj = fileVersionObj.parent
            fileObj.deleteChildren(session)
            fileModified = True

    # Create a new dataset version if:
    # - a file has been added, replaced, or deleted, and
    # - the current version is the latest
    createNewDatasetVersion = haveLatestDsetVersion and fileModified
    
    return createNewDatasetVersion, newFileVersionObjs

def renameFilesVersion(dset, dsetVersion, pathlist, session, cfHandler, configOptions, aggregateDimensionName=None, offline=False, progressCallback=None, stopEvent=None, keepVersion=False, newVersion=None, extraFields=None, **context):

    info("Renaming files in dataset: %s, version %d"%(dset.name, dsetVersion.version))

    # Get the list of FileVersion objects for this version
    locdict = {}
    todelete = {}
    for fobj in dsetVersion.getFileVersions():
        loc = fobj.location
        locdict[loc] = todelete[loc] = fobj

    basedict = dset.getBaseDictionary()

    nfiles = len(pathlist)

    varlocate = configOptions['variable_locate']
    seq = 0
    for path, size in pathlist:

        # If the file exists, rename it
        oldpath = None
        if extraFields is not None:
            oldpath = extraFieldsGet(extraFields, (dset.name, path, 'from_file'), dsetVersion)
        if oldpath is None:
            info("No from_file field for file %s, skipping"%path)
            continue

        if locdict.has_key(oldpath):
            fileVersionObj = locdict[oldpath]
            fileObj = fileVersionObj.parent
            if not os.path.exists(path):
                info("File not found: %s, skipping"%path)
                continue
            info("Renaming %s to %s"%(oldpath, path))
            del basedict[fileObj.base]
            base = generateFileBase(path, basedict, dset.name)
            fileObj.base = base
            basedict[base] = 1
            fileVersionObj.location = path
            del locdict[oldpath]
            locdict[path] = fileVersionObj
        else:
            info("File entry %s not found, skipping"%oldpath)
            continue

        seq += 1

        # Callback progress
        try:
            issueCallback(progressCallback, seq, nfiles, 0, 1, stopEvent=stopEvent)
        except:
            session.rollback()
            session.close()
            raise

    return False

def deleteFilesVersion(dset, dsetVersion, pathlist, session, cfHandler, configOptions, aggregateDimensionName=None, offline=False, progressCallback=None, stopEvent=None, extraFields=None, **context):

    info("Deleting file entries for dataset: %s, version %d"%(dset.name, dsetVersion.version))

    haveLatestDsetVersion = (dsetVersion.version == dset.getVersion())

    # Create a file dictionary for the dataset
    fobjdict = {}                       # file version objects for the new dataset version
    for fobj in dsetVersion.getFileVersions():
        fobjdict[fobj.location] = fobj

    nfiles = len(pathlist)

    varlocate = configOptions['variable_locate']
    seq = 0
    addNewDatasetVersion = False
    for path, size in pathlist:

        # If the file exists in the dataset, delete the file children (with cascade), and the file
        if fobjdict.has_key(path):
            fileVersionObj = fobjdict[path]
            info("Deleting entry for file %s"%path)

            # If this is the latest dataset version, remove the file variables and reaggregate ...
            if haveLatestDsetVersion:
                fileVersionObj.parent.deleteChildren(session)
                addNewDatasetVersion = True

            # ... otherwise just delete the membership of the file version in the dataset version
            else:
                fileVersionObj.deleteChildren(session)
                session.commit()
            del fobjdict[path]
        else:
            info("File entry not found: %s, skipping"%path)

        seq += 1

        # Callback progress
        try:
            issueCallback(progressCallback, seq, nfiles, 0, 1, stopEvent=stopEvent)
        except:
            session.rollback()
            session.close()
            raise

    return addNewDatasetVersion, fobjdict.values()

def extractFromFile(dataset, openfile, fileobj, session, cfHandler, aggdimName=None, varlocate=None, **context):
    """
    Extract metadata from a file, add to a database.

    dataset
      The dataset instance.

    openfile
      An open netCDF file object.

    fileobj
      A (logical) file instance.

    session
      A database session instance.

    cfHandler
      A CF handler instance

    aggdimName
      The name of the dimension which is split across files, if any.

    varlocate
      List with elements [varname, pattern]. The variable will be extracted from the file only if the filename
      matches the pattern at the start. Example: [['ps', 'ps_'], ['xyz', 'xyz_']]

    context
      A dictionary with keys project, model, experiment, and run.

    """

    fileVersion = fileobj.versions[-1]

    # Get the aggregate dimension range
    if aggdimName is not None and openfile.hasVariable(aggdimName):
        aggvarFirst = openfile.getVariable(aggdimName, index=0)
        aggvarLast = openfile.getVariable(aggdimName, index=-1)
        aggvarLen = openfile.inquireVariableShape(aggdimName)[0]
        aggvarunits = map_to_charset(openfile.getAttribute("units", aggdimName))
        if aggdimName.lower()=="time" or (openfile.hasAttribute("axis", aggdimName) and openfile.getAttribute("axis", aggdimName)=="T"):
            if abs(aggvarFirst)>1.e12 or abs(aggvarLast)>1.e12:
                dataset.warning("File: %s has time range: [%f, %f], looks bogus."%(fileVersion.location, aggvarFirst, aggvarLast), WARNING_LEVEL, AGGREGATE_MODULE)

    if aggdimName is not None and not openfile.hasVariable(aggdimName):
        info("Aggregate dimension not found: %s"%aggdimName)

    varlocatedict = {}
    if varlocate is not None:
        for varname, pattern in varlocate:
            varlocatedict[varname] = pattern

    # For each variable in the file:
    for varname in openfile.inquireVariableList():
        varshape = openfile.inquireVariableShape(varname)
        debug("%s%s"%(varname, `varshape`))

        # Check varlocate
        if varlocatedict.has_key(varname) and not re.match(varlocatedict[varname], os.path.basename(fileVersion.location)):
            debug("Skipping variable %s in %s"%(varname, fileVersion.location))
            continue

        # Create a file variable
        filevar = FileVariable(varname, openfile.getAttribute('long_name', varname, None))
        fileobj.file_variables.append(filevar)

        # Create attributes:
        for attname in openfile.inquireAttributeList(varname):
            attvalue = openfile.getAttribute(attname, varname)
            atttype, attlen = getTypeAndLen(attvalue)
            attribute = FileVariableAttribute(attname, map_to_charset(attvalue), atttype, attlen)
            filevar.attributes.append(attribute)
            debug('  %s.%s = %s'%(varname, attname, `attvalue`))

        # Create dimensions
        seq = 0
        dimensionList = openfile.inquireVariableDimensions(varname)
        for dimname, dimlen in zip(dimensionList, varshape):
            dimension = FileVariableDimension(dimname, dimlen, seq)
            filevar.dimensions.append(dimension)
            if dimname==aggdimName:
                filevar.aggdim_first = aggvarFirst
                filevar.aggdim_last = aggvarLast
                filevar.aggdim_units = aggvarunits
            seq += 1

        # Set coordinate axis range and type if applicable
        if len(varshape)==1:
            var0 = openfile.getVariable(varname, index=0)
            varn = openfile.getVariable(varname, index=-1)
            if cfHandler.axisIsLatitude(filevar):
                filevar.coord_range = '%f:%f'%(var0, varn)
                filevar.coord_type = 'Y'
            elif cfHandler.axisIsLongitude(filevar):
                filevar.coord_range = '%f:%f'%(var0, varn)
                filevar.coord_type = 'X'
            elif cfHandler.axisIsLevel(filevar):
                vararray = openfile.getVariable(varname)
                filevar.coord_range = '%f:%f'%(var0, varn)
                filevar.coord_type = 'Z'
                filevar.coord_values = str(vararray)[1:-1] # See set_printoptions call above

    # Create global attribute
    for attname in openfile.inquireAttributeList():
        attvalue = openfile.getAttribute(attname, None)
        atttype, attlen = getTypeAndLen(attvalue)
        attribute = FileAttribute(attname, map_to_charset(attvalue), atttype, attlen)
        fileobj.attributes.append(attribute)
        if attname=='tracking_id':
            fileVersion.tracking_id = attvalue
        debug('.%s = %s'%(attname, attvalue))

def lookupVar(name, index):
    """Helper function for aggregateVariables:
    Lookup a variable in the dataset index."""
    varlist = index.get(name, None)
    if varlist is None:
        result = None
    else:
        result = varlist[0][0]
    return result

def lookupCoord(name, index, length):
    """Helper function for aggregateVariables:
    Lookup a coordinate variable in the dataset index."""
    varlist = index.get(name, None)
    if varlist is None:
        result = None
    else:
        for var, domain in varlist:
            if len(domain)>0:
                dlen = domain[0][1]
            else:
                dlen = 0
            
            if dlen==length:
                result = var
                break
        else:
            result = None
    return result

def lookupAttr(var, attname):
    """Helper function for aggregateVariables:
    Lookup an attribute of the variable."""
    result = None
    if var is not None:
        for attr in var.attributes:
            if attr.name==attname:
                result = attr.value
                break
    return result

def createAggregateVar(var, varattr, aggregateDimensionName):
    """Helper function for aggregateVariables:
    Create an aggregate dimension or bounds variable associated with a variable."""
    aggVar = None
    for filevar in var.file_variables:
        if hasattr(filevar.file, varattr):
            aggfilevar = getattr(filevar.file, varattr)
            if aggVar is None:
                aggVar = Variable(aggfilevar.short_name, aggfilevar.long_name)
                aggVar.domain = ((aggregateDimensionName, 0, 0),)+tuple(aggfilevar.domain[1:]) # Zero out aggregate dimension length
                # Create attributes
                for fvattribute in aggfilevar.attributes:
                    attribute = VariableAttribute(fvattribute.name, map_to_charset(fvattribute.value), fvattribute.datatype, fvattribute.length)
                    aggVar.attributes.append(attribute)

            aggVar.file_variables.append(aggfilevar)
    return aggVar

def aggregateVariables(datasetName, dbSession, aggregateDimensionName=None, cfHandler=None, progressCallback=None, stopEvent=None, datasetInstance=None):
    """
    Aggregate file variables into variables, and add to the database. Populates the database tables:

    - variable
    - file_variable
    - associated attribute tables

    Returns a Dataset object.

    datasetName
      String dataset identifier.

    dbSession
      A database Session.

    aggregateDimensionName
      The name of the dimension across which the dataset is aggregated, if any.

    cfHandler
      A CFHandler to validate standard names, etc.

    progressCallback
      Tuple (callback, initial, final) where ``callback`` is a function of the form ``callback(progress)``, ``initial`` is the initial value reported, ``final`` is the final value reported.

    stopEvent
      Object with boolean attribute ``stop_extract`` (for example, ``utility.StopEvent``). If set to True (in another thread) the extraction is stopped.

    datasetInstance
      Existing dataset instance. If not provided, the instance is regenerated from the database.

    """

    session = dbSession()
    info("Aggregating variables")

    # Lookup the dataset
    if datasetInstance is None:
        dset = session.query(Dataset).filter_by(name=datasetName).first()
        for variable in dset.variables:
            session.delete(variable)
        for attrname, attr in dset.attributes.items():
            if not attr.is_category:
                del dset.attributes[attrname]
        session.commit()
        dset.variables = []
    else:
        dset = datasetInstance
        # session.save_or_update(dset)
        session.add(dset)
    if dset is None:
        raise ESGPublishError("Dataset not found: %s"%datasetName)

    dsetindex = {}                      # dsetindex[varname] = [(variable, domain), (variable, domain), ...]
                                        #   where domain = ((dim0, len0, 0), (dim1, len1, 1), ...)
                                        #   Note:
                                        #     (1) If a dim0 is the aggregate dimension, len0 is 0
                                        #     (2) A dsetindex entry will only have multiple tuples if
                                        #         there are more than one variable with the same name
                                        #         and different domains.
    varindex = {}                       # varindex[(varname, domain, attrname)] = attribute
    globalAttrIndex = {}                # globalAttrIndex[attname] = attval, for global attributes
    dsetvars = []

    # Create variables
    seq = 0
    nfiles = len(dset.getFiles())
    for file in dset.getFiles():
        for filevar in file.file_variables:

            # Get the filevar and variable domain
            fvdomain = map(lambda x: (x.name, x.length, x.seq), filevar.dimensions)
            fvdomain.sort(lambda x,y: cmp(x[SEQ], y[SEQ]))
            filevar.domain = fvdomain
            if len(fvdomain)>0 and fvdomain[0][0]==aggregateDimensionName:
                vardomain = ((aggregateDimensionName, 0, 0),)+tuple(fvdomain[1:]) # Zero out aggregate dimension length
            else:
                vardomain = tuple(fvdomain)

            # Create the variable if necessary
            varlist = dsetindex.get(filevar.short_name, None)
            if varlist is None or vardomain not in [item[1] for item in varlist]:
                var = Variable(filevar.short_name, filevar.long_name)
                var.domain = vardomain

                # Record coordinate variable range if applicable
                if filevar.coord_type is not None:
                    var.coord_type = filevar.coord_type
                    if var.coord_type=='Z':
                        var.coord_values = filevar.coord_values
                    var.coord_range = filevar.coord_range
                    
                dsetvars.append(var)
                if varlist is None:
                    dsetindex[var.short_name] = [(var, vardomain)]
                else:
                    varlist.append((var, vardomain))
            else:
                for tvar, domain in varlist:
                    if domain==vardomain:
                        var = tvar
                        break

            # Attach the file variable to the variable
            var.file_variables.append(filevar)

            # Create attributes
            for fvattribute in filevar.attributes:
                vattribute = varindex.get((var.short_name, vardomain, fvattribute.name), None)
                if vattribute is None:
                    attribute = VariableAttribute(fvattribute.name, map_to_charset(fvattribute.value), fvattribute.datatype, fvattribute.length)
                    var.attributes.append(attribute)
                    varindex[(var.short_name, vardomain, attribute.name)] = attribute
                    if attribute.name == 'units':
                        var.units = attribute.value

        # Create global attributes
        for fileattr in file.attributes:
            fattribute = globalAttrIndex.get(fileattr.name, None)
            if fattribute is None and fileattr.name not in ['readDimension']:
                attribute = DatasetAttribute(fileattr.name, map_to_charset(fileattr.value), fileattr.datatype, fileattr.length)
                dset.attributes[attribute.name] = attribute
                globalAttrIndex[attribute.name] = attribute
        seq += 1
        try:
            issueCallback(progressCallback, seq, nfiles, 0, 0.25, stopEvent=stopEvent)
        except:
            session.rollback()
            session.close()
            raise

    # Find the aggregation dimension bounds variable, if any
    aggDim = lookupVar(aggregateDimensionName, dsetindex)
    boundsName = lookupAttr(aggDim, 'bounds')
    aggUnits = lookupAttr(aggDim, 'units')
    aggDimBounds = lookupVar(boundsName, dsetindex)

    # Set calendar for time aggregation
    isTime = cfHandler.axisIsTime(aggDim)
    if isTime:
        calendar = cfHandler.getCalendarTag(aggDim)
        if calendar is None:
            calendar = "gregorian"
    else:
        calendar = None
    dset.calendar = calendar
    dset.aggdim_name = aggregateDimensionName
    dset.aggdim_units = aggUnits
    cdcalendar = cfHandler.tagToCalendar(calendar)

    # Add the non-aggregate dimension variables to the dataset
    for var in dsetvars:
        if var not in [aggDim, aggDimBounds]:
            dset.variables.append(var)

    # Set coordinate ranges
    for var in dset.variables:
        for name, length, seq in var.domain:
            if name==aggregateDimensionName:
                continue
            dvar = lookupCoord(name, dsetindex, length)
            if dvar is not None:
                units = lookupAttr(dvar, 'units')
                if hasattr(dvar, 'coord_type'):
                    if dvar.coord_type=='X':
                        var.eastwest_range = dvar.coord_range+':'+units
                    elif dvar.coord_type=='Y':
                        var.northsouth_range = dvar.coord_range+':'+units
                    elif dvar.coord_type=='Z':
                        var.updown_range = dvar.coord_range+':'+units
                        var.updown_values = dvar.coord_values

    # Attach aggregate dimension filevars to files
    if aggDim is not None:
        for filevar in aggDim.file_variables:
            filevar.file.aggDim = filevar
    if aggDimBounds is not None:
        for filevar in aggDimBounds.file_variables:
            filevar.file.aggDimBounds = filevar

    # Combine aggregate dimensions:
    # Scan all variables with the aggregate dimension in the domain. For each such variable,
    # create an aggregate dimension variable, and bounds if needed.
    timevars = []
    for var in dset.variables:
        if len(var.domain)>0 and aggregateDimensionName==var.domain[0][NAME]:
            aggVar = createAggregateVar(var, 'aggDim', aggregateDimensionName)
            aggBoundsVar = createAggregateVar(var, 'aggDimBounds', aggregateDimensionName)
            if aggVar is not None:
                aggVar.units = aggUnits
                timevars.append(aggVar)
            if aggBoundsVar is not None:
                timevars.append(aggBoundsVar)

    # Create variable dimensions, aggregating the agg dimension
    debug("Creating dimensions")
    i = 0
    nvars = len(dset.variables+timevars)
    for var in dset.variables+timevars:
        vardomain = var.domain

        # Increment aggregate dimension length
        if len(vardomain)>0 and aggregateDimensionName==vardomain[0][NAME]:
            for filevar in var.file_variables:
                fvdomain = filevar.domain
                vardomain = ((aggregateDimensionName, vardomain[0][LENGTH]+fvdomain[0][LENGTH], vardomain[0][SEQ]),)+tuple(vardomain[1:])
        var.domain = vardomain

        # Create the variable domain
        for name, length, seq in vardomain:
            dimension = VariableDimension(name, length, seq)
            var.dimensions.append(dimension)
        i += 1
        try:
            issueCallback(progressCallback, i, nvars, 0.25, 0.5, stopEvent=stopEvent)
        except:
            session.rollback()
            session.close()
            raise

    # Set variable aggregate dimension ranges
    debug("Setting aggregate dimension ranges")
    seq = 0
    nvars = len(dset.variables+timevars)
    for var in dset.variables+timevars:
        vardomain = var.domain
        if len(vardomain)>0 and vardomain[0][NAME]==aggregateDimensionName:

            # Adjust times so they have consistent base units
            try:
                filevarRanges = map(lambda x: (x.file.getLocation(),
                                               cfHandler.normalizeTime(x.aggdim_first, x.aggdim_units, aggUnits, calendar=cdcalendar),
                                               cfHandler.normalizeTime(x.aggdim_last, x.aggdim_units, aggUnits, calendar=cdcalendar)),
                                    var.file_variables)
            except:
                for fv in var.file_variables:
                    try:
                        firstt = cfHandler.normalizeTime(fv.aggdim_first, fv.aggdim_units, aggUnits, calendar=cdcalendar)
                        lastt = cfHandler.normalizeTime(fv.aggdim_last, fv.aggdim_units, aggUnits, calendar=cdcalendar)
                    except:
                        error("path=%s, Invalid aggregation dimension value or units: first_value=%f, last_value=%f, units=%s"%(fv.file.getLocation(), fv.aggdim_first, fv.aggdim_last, fv.aggdim_units))
                        raise

            mono = cmp(filevarRanges[0][1], filevarRanges[0][2])
            if mono<=0:
                filevarRanges.sort(lambda x, y: cmp(x[1], y[1]))
            else:
                filevarRanges.sort(lambda x, y: -cmp(x[1], y[1]))

            # Check that ranges don't overlap. Aggregate dimension and bounds may be duplicated.
            lastValues = numpy.array(map(lambda x: x[2], filevarRanges))
            firstValues = numpy.array(map(lambda x: x[1], filevarRanges))
            if (var not in [aggDim, aggDimBounds]):
                if mono<=0:
                    compare = (lastValues[0:-1] >= firstValues[1:])
                else:
                    compare = (lastValues[0:-1] <= firstValues[1:])
                if compare.any():
                    overlaps = compare.nonzero()[0]
                    dset.warning("Variable %s is duplicated:"%(var.short_name), WARNING_LEVEL, AGGREGATE_MODULE)
                    var.has_errors = True
                    nprint = min(len(overlaps), 3)
                    for i in range(nprint):
                        dset.warning("  %s: (%d, %d)"%filevarRanges[overlaps[i]], WARNING_LEVEL, AGGREGATE_MODULE)
                        dset.warning("  %s: (%d, %d)"%filevarRanges[overlaps[i]+1], WARNING_LEVEL, AGGREGATE_MODULE)
                    if len(overlaps)>nprint:
                        dset.warning("    ... (%d duplications total)"%len(overlaps), WARNING_LEVEL, AGGREGATE_MODULE)

                # Check monotonicity of last values.
                else:
                    if mono<=0:
                        compare = (lastValues[0:-1] < lastValues[1:]).all()
                    else:
                        compare = (lastValues[0:-1] > lastValues[1:]).all()
                    if not compare:
                        dset.warning("File aggregate dimension ranges are not monotonic for variable %s: %s"%(var.short_name, `filevarRanges`), WARNING_LEVEL, AGGREGATE_MODULE)
                        var.has_errors = True

            var.aggdim_first = firstValues[0]
            var.aggdim_last = lastValues[-1]
        seq += 1
        try:
            issueCallback(progressCallback, seq, nvars, 0.5, 0.75, stopEvent=stopEvent)
        except:
            session.rollback()
            session.close()
            raise

    # Combine identical aggregate dimensions and add to the dataset
    timevardict = {}
    for var in timevars:
        timevardict[(var.short_name, var.domain, var.aggdim_first, var.aggdim_last)] = var

    for var in timevardict.values():
        dset.variables.append(var)
        
    # Validate standard names
    seq = 0
    nvars = len(dset.variables)
    for var in dset.variables:
        attr = lookupAttr(var, 'standard_name')
        if (attr is not None):
            if (cfHandler is not None) and (not cfHandler.validateStandardName(attr)):
                info("Invalid standard name: %s for variable %s"%(attr, var.short_name))
            else:
                var.standard_name = attr
        seq += 1
        try:
            issueCallback(progressCallback, seq, nvars, 0.75, 1.0, stopEvent=stopEvent)
        except:
            session.rollback()
            session.close()
            raise

    debug("Adding variable info to database")
    session.commit()
    session.close()
