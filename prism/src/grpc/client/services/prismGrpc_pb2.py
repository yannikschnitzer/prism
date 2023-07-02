# -*- coding: utf-8 -*-
# Generated by the protocol buffer compiler.  DO NOT EDIT!
# source: prismGrpc.proto
"""Generated protocol buffer code."""
from google.protobuf.internal import builder as _builder
from google.protobuf import descriptor as _descriptor
from google.protobuf import descriptor_pool as _descriptor_pool
from google.protobuf import symbol_database as _symbol_database
# @@protoc_insertion_point(imports)

_sym_db = _symbol_database.Default()




DESCRIPTOR = _descriptor_pool.Default().AddSerializedFile(b'\n\x0fprismGrpc.proto\"A\n\rUploadRequest\x12\x12\n\x08\x66ilename\x18\x02 \x01(\tH\x00\x12\x14\n\nchunk_data\x18\x03 \x01(\x0cH\x00\x42\x06\n\x04\x64\x61ta\"\x1f\n\x0bUploadReply\x12\x10\n\x08\x66ilename\x18\x01 \x01(\t\"\x11\n\x0fPrismDevNullLog\"\x1c\n\x0cPrismFileLog\x12\x0c\n\x04type\x18\x01 \x01(\t\"c\n\x08PrismLog\x12(\n\x0c\x64\x65v_null_log\x18\x01 \x01(\x0b\x32\x10.PrismDevNullLogH\x00\x12!\n\x08\x66ile_log\x18\x02 \x01(\x0b\x32\r.PrismFileLogH\x00\x42\n\n\x08log_type\"D\n\x11InitialiseRequest\x12\x17\n\x0fprism_object_id\x18\x01 \x01(\t\x12\x16\n\x03log\x18\x02 \x01(\x0b\x32\t.PrismLog\"=\n\x12InitialiseResponse\x12\x17\n\x0fprism_object_id\x18\x01 \x01(\t\x12\x0e\n\x06status\x18\x02 \x01(\t\"J\n\x15LoadPRISMModelRequest\x12\x17\n\x0fprism_object_id\x18\x01 \x01(\t\x12\x18\n\x10module_object_id\x18\x02 \x01(\t\"[\n\x16LoadPRISMModelResponse\x12\x17\n\x0fprism_object_id\x18\x01 \x01(\t\x12\x18\n\x10module_object_id\x18\x02 \x01(\t\x12\x0e\n\x06status\x18\x03 \x01(\t\"\x90\x01\n\x1aParsePropertiesFileRequest\x12\x17\n\x0fprism_object_id\x18\x01 \x01(\t\x12\x18\n\x10module_object_id\x18\x02 \x01(\t\x12!\n\x19properties_file_object_id\x18\x03 \x01(\t\x12\x1c\n\x14properties_file_name\x18\x04 \x01(\t\"Z\n\x1bParsePropertiesFileResponse\x12\x17\n\x0fprism_object_id\x18\x01 \x01(\t\x12\x0e\n\x06status\x18\x02 \x01(\t\x12\x12\n\nproperties\x18\x03 \x01(\t\"\x85\x01\n\x11ModelCheckRequest\x12\x17\n\x0fprism_object_id\x18\x01 \x01(\t\x12!\n\x19properties_file_object_id\x18\x02 \x01(\t\x12\x1a\n\x12property_object_id\x18\x03 \x01(\t\x12\x18\n\x10result_object_id\x18\x04 \x01(\t\"4\n\x12ModelCheckResponse\x12\x0e\n\x06result\x18\x01 \x01(\t\x12\x0e\n\x06status\x18\x02 \x01(\t\"L\n\x18ParseAndLoadModelRequest\x12\x17\n\x0fprism_object_id\x18\x01 \x01(\t\x12\x17\n\x0fmodel_file_name\x18\x02 \x01(\t\"A\n\x16ParseAndLoadModelReply\x12\x17\n\x0fprism_object_id\x18\x01 \x01(\t\x12\x0e\n\x06status\x18\x02 \x01(\t\",\n\x11\x43losePrismRequest\x12\x17\n\x0fprism_object_id\x18\x01 \x01(\t\"=\n\x12\x43losePrismResponse\x12\x17\n\x0fprism_object_id\x18\x01 \x01(\t\x12\x0e\n\x06status\x18\x02 \x01(\t\"c\n\x15ParseModelFileRequest\x12\x17\n\x0fprism_object_id\x18\x01 \x01(\t\x12\x18\n\x10module_object_id\x18\x02 \x01(\t\x12\x17\n\x0fmodel_file_name\x18\x03 \x01(\t\"[\n\x16ParseModelFileResponse\x12\x17\n\x0fprism_object_id\x18\x01 \x01(\t\x12\x18\n\x10module_object_id\x18\x02 \x01(\t\x12\x0e\n\x06status\x18\x03 \x01(\t\"q\n\x18GetPropertyObjectRequest\x12!\n\x19properties_file_object_id\x18\x01 \x01(\t\x12\x1a\n\x12property_object_id\x18\x02 \x01(\t\x12\x16\n\x0eproperty_index\x18\x03 \x01(\x05\"`\n\x19GetPropertyObjectResponse\x12!\n\x19properties_file_object_id\x18\x01 \x01(\t\x12\x0e\n\x06status\x18\x02 \x01(\t\x12\x10\n\x08property\x18\x03 \x01(\t\"y\n&PropertiesFileForwardMethodCallRequest\x12!\n\x19properties_file_object_id\x18\x01 \x01(\t\x12\x0e\n\x06method\x18\x02 \x01(\t\x12\x0c\n\x04\x61rgs\x18\x03 \x01(\t\x12\x0e\n\x06kwargs\x18\x04 \x01(\t\"l\n\'PropertiesFileForwardMethodCallResponse\x12!\n\x19properties_file_object_id\x18\x01 \x01(\t\x12\x0e\n\x06status\x18\x02 \x01(\t\x12\x0e\n\x06result\x18\x03 \x01(\t\"k\n*GetUndefinedConstantsUsedInPropertyRequest\x12!\n\x19properties_file_object_id\x18\x01 \x01(\t\x12\x1a\n\x12property_object_id\x18\x02 \x01(\t\"P\n+GetUndefinedConstantsUsedInPropertyResponse\x12\x0e\n\x06status\x18\x01 \x01(\t\x12\x11\n\tconstants\x18\x02 \x03(\t\"N\n\x0f\x41\x64\x64ValueRequest\x12\x18\n\x10values_object_id\x18\x01 \x01(\t\x12\x12\n\nconst_name\x18\x02 \x01(\t\x12\r\n\x05value\x18\x03 \x01(\x05\"2\n\x10\x41\x64\x64ValueResponse\x12\x0e\n\x06result\x18\x01 \x01(\t\x12\x0e\n\x06status\x18\x02 \x01(\t\"_\n SetSomeUndefinedConstantsRequest\x12!\n\x19properties_file_object_id\x18\x01 \x01(\t\x12\x18\n\x10values_object_id\x18\x02 \x01(\t\"3\n!SetSomeUndefinedConstantsResponse\x12\x0e\n\x06status\x18\x01 \x01(\t\"(\n\x13\x44\x65leteObjectRequest\x12\x11\n\tobject_id\x18\x01 \x01(\t\"&\n\x14\x44\x65leteObjectResponse\x12\x0e\n\x06status\x18\x01 \x01(\t\"\x9f\x01\n\x1dInitUndefinedConstantsRequest\x12\x18\n\x10module_object_id\x18\x01 \x01(\t\x12!\n\x19properties_file_object_id\x18\x02 \x01(\t\x12\x1a\n\x12property_object_id\x18\x03 \x01(\t\x12%\n\x1dundefined_constants_object_id\x18\x04 \x01(\t\"0\n\x1eInitUndefinedConstantsResponse\x12\x0e\n\x06status\x18\x01 \x01(\t2\xcc\x08\n\x11PrismProtoService\x12=\n\x0c\x44\x65leteObject\x12\x14.DeleteObjectRequest\x1a\x15.DeleteObjectResponse\"\x00\x12v\n\x1fPropertiesFileForwardMethodCall\x12\'.PropertiesFileForwardMethodCallRequest\x1a(.PropertiesFileForwardMethodCallResponse\"\x00\x12.\n\nUploadFile\x12\x0e.UploadRequest\x1a\x0c.UploadReply\"\x00(\x01\x12\x37\n\nInitialise\x12\x12.InitialiseRequest\x1a\x13.InitialiseResponse\"\x00\x12\x43\n\x0eParseModelFile\x12\x16.ParseModelFileRequest\x1a\x17.ParseModelFileResponse\"\x00\x12\x43\n\x0eLoadPRISMModel\x12\x16.LoadPRISMModelRequest\x1a\x17.LoadPRISMModelResponse\"\x00\x12R\n\x13ParsePropertiesFile\x12\x1b.ParsePropertiesFileRequest\x1a\x1c.ParsePropertiesFileResponse\"\x00\x12L\n\x11GetPropertyObject\x12\x19.GetPropertyObjectRequest\x1a\x1a.GetPropertyObjectResponse\"\x00\x12\x37\n\nModelCheck\x12\x12.ModelCheckRequest\x1a\x13.ModelCheckResponse\"\x00\x12\x82\x01\n#GetUndefinedConstantsUsedInProperty\x12+.GetUndefinedConstantsUsedInPropertyRequest\x1a,.GetUndefinedConstantsUsedInPropertyResponse\"\x00\x12\x31\n\x08\x41\x64\x64Value\x12\x10.AddValueRequest\x1a\x11.AddValueResponse\"\x00\x12\x64\n\x19SetSomeUndefinedConstants\x12!.SetSomeUndefinedConstantsRequest\x1a\".SetSomeUndefinedConstantsResponse\"\x00\x12[\n\x16InitUndefinedConstants\x12\x1e.InitUndefinedConstantsRequest\x1a\x1f.InitUndefinedConstantsResponse\"\x00\x12\x37\n\nClosePrism\x12\x12.ClosePrismRequest\x1a\x13.ClosePrismResponse\"\x00\x42\x16\n\x14grpc.server.servicesb\x06proto3')

_builder.BuildMessageAndEnumDescriptors(DESCRIPTOR, globals())
_builder.BuildTopDescriptorsAndMessages(DESCRIPTOR, 'prismGrpc_pb2', globals())
if _descriptor._USE_C_DESCRIPTORS == False:

  DESCRIPTOR._options = None
  DESCRIPTOR._serialized_options = b'\n\024grpc.server.services'
  _UPLOADREQUEST._serialized_start=19
  _UPLOADREQUEST._serialized_end=84
  _UPLOADREPLY._serialized_start=86
  _UPLOADREPLY._serialized_end=117
  _PRISMDEVNULLLOG._serialized_start=119
  _PRISMDEVNULLLOG._serialized_end=136
  _PRISMFILELOG._serialized_start=138
  _PRISMFILELOG._serialized_end=166
  _PRISMLOG._serialized_start=168
  _PRISMLOG._serialized_end=267
  _INITIALISEREQUEST._serialized_start=269
  _INITIALISEREQUEST._serialized_end=337
  _INITIALISERESPONSE._serialized_start=339
  _INITIALISERESPONSE._serialized_end=400
  _LOADPRISMMODELREQUEST._serialized_start=402
  _LOADPRISMMODELREQUEST._serialized_end=476
  _LOADPRISMMODELRESPONSE._serialized_start=478
  _LOADPRISMMODELRESPONSE._serialized_end=569
  _PARSEPROPERTIESFILEREQUEST._serialized_start=572
  _PARSEPROPERTIESFILEREQUEST._serialized_end=716
  _PARSEPROPERTIESFILERESPONSE._serialized_start=718
  _PARSEPROPERTIESFILERESPONSE._serialized_end=808
  _MODELCHECKREQUEST._serialized_start=811
  _MODELCHECKREQUEST._serialized_end=944
  _MODELCHECKRESPONSE._serialized_start=946
  _MODELCHECKRESPONSE._serialized_end=998
  _PARSEANDLOADMODELREQUEST._serialized_start=1000
  _PARSEANDLOADMODELREQUEST._serialized_end=1076
  _PARSEANDLOADMODELREPLY._serialized_start=1078
  _PARSEANDLOADMODELREPLY._serialized_end=1143
  _CLOSEPRISMREQUEST._serialized_start=1145
  _CLOSEPRISMREQUEST._serialized_end=1189
  _CLOSEPRISMRESPONSE._serialized_start=1191
  _CLOSEPRISMRESPONSE._serialized_end=1252
  _PARSEMODELFILEREQUEST._serialized_start=1254
  _PARSEMODELFILEREQUEST._serialized_end=1353
  _PARSEMODELFILERESPONSE._serialized_start=1355
  _PARSEMODELFILERESPONSE._serialized_end=1446
  _GETPROPERTYOBJECTREQUEST._serialized_start=1448
  _GETPROPERTYOBJECTREQUEST._serialized_end=1561
  _GETPROPERTYOBJECTRESPONSE._serialized_start=1563
  _GETPROPERTYOBJECTRESPONSE._serialized_end=1659
  _PROPERTIESFILEFORWARDMETHODCALLREQUEST._serialized_start=1661
  _PROPERTIESFILEFORWARDMETHODCALLREQUEST._serialized_end=1782
  _PROPERTIESFILEFORWARDMETHODCALLRESPONSE._serialized_start=1784
  _PROPERTIESFILEFORWARDMETHODCALLRESPONSE._serialized_end=1892
  _GETUNDEFINEDCONSTANTSUSEDINPROPERTYREQUEST._serialized_start=1894
  _GETUNDEFINEDCONSTANTSUSEDINPROPERTYREQUEST._serialized_end=2001
  _GETUNDEFINEDCONSTANTSUSEDINPROPERTYRESPONSE._serialized_start=2003
  _GETUNDEFINEDCONSTANTSUSEDINPROPERTYRESPONSE._serialized_end=2083
  _ADDVALUEREQUEST._serialized_start=2085
  _ADDVALUEREQUEST._serialized_end=2163
  _ADDVALUERESPONSE._serialized_start=2165
  _ADDVALUERESPONSE._serialized_end=2215
  _SETSOMEUNDEFINEDCONSTANTSREQUEST._serialized_start=2217
  _SETSOMEUNDEFINEDCONSTANTSREQUEST._serialized_end=2312
  _SETSOMEUNDEFINEDCONSTANTSRESPONSE._serialized_start=2314
  _SETSOMEUNDEFINEDCONSTANTSRESPONSE._serialized_end=2365
  _DELETEOBJECTREQUEST._serialized_start=2367
  _DELETEOBJECTREQUEST._serialized_end=2407
  _DELETEOBJECTRESPONSE._serialized_start=2409
  _DELETEOBJECTRESPONSE._serialized_end=2447
  _INITUNDEFINEDCONSTANTSREQUEST._serialized_start=2450
  _INITUNDEFINEDCONSTANTSREQUEST._serialized_end=2609
  _INITUNDEFINEDCONSTANTSRESPONSE._serialized_start=2611
  _INITUNDEFINEDCONSTANTSRESPONSE._serialized_end=2659
  _PRISMPROTOSERVICE._serialized_start=2662
  _PRISMPROTOSERVICE._serialized_end=3762
# @@protoc_insertion_point(module_scope)
