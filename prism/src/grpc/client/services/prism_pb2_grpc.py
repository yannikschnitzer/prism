# Generated by the gRPC Python protocol compiler plugin. DO NOT EDIT!
"""Client and server classes corresponding to protobuf-defined services."""
import grpc

import prismGrpc_pb2_grpc as prism__pb2


class PrismProtoServiceStub(object):
    """Service Definitions
    """

    def __init__(self, channel):
        """Constructor.

        Args:
            channel: A grpc.Channel.
        """
        self.UploadFile = channel.stream_unary(
                '/PrismProtoService/UploadFile',
                request_serializer=prism__pb2.UploadRequest.SerializeToString,
                response_deserializer=prism__pb2.UploadReply.FromString,
                )
        self.Initialise = channel.unary_unary(
                '/PrismProtoService/Initialise',
                request_serializer=prism__pb2.InitialiseRequest.SerializeToString,
                response_deserializer=prism__pb2.InitialiseResponse.FromString,
                )
        self.ParseAndLoadModel = channel.unary_unary(
                '/PrismProtoService/ParseAndLoadModel',
                request_serializer=prism__pb2.ParseAndLoadModelRequest.SerializeToString,
                response_deserializer=prism__pb2.ParseAndLoadModelReply.FromString,
                )
        self.LoadPRISMModel = channel.unary_unary(
                '/PrismProtoService/LoadPRISMModel',
                request_serializer=prism__pb2.LoadPRISMModelRequest.SerializeToString,
                response_deserializer=prism__pb2.LoadPRISMModelResponse.FromString,
                )
        self.ParsePropertiesFile = channel.unary_unary(
                '/PrismProtoService/ParsePropertiesFile',
                request_serializer=prism__pb2.ParsePropertiesFileRequest.SerializeToString,
                response_deserializer=prism__pb2.ParsePropertiesFileResponse.FromString,
                )
        self.DefineUndefinedConstants = channel.unary_unary(
                '/PrismProtoService/DefineUndefinedConstants',
                request_serializer=prism__pb2.DefineUndefinedConstantsRequest.SerializeToString,
                response_deserializer=prism__pb2.DefineUndefinedConstantsResponse.FromString,
                )
        self.ModelCheck = channel.unary_unary(
                '/PrismProtoService/ModelCheck',
                request_serializer=prism__pb2.ModelCheckRequest.SerializeToString,
                response_deserializer=prism__pb2.ModelCheckResponse.FromString,
                )
        self.ModelCheckWithConstants = channel.unary_unary(
                '/PrismProtoService/ModelCheckWithConstants',
                request_serializer=prism__pb2.ModelCheckWithConstantsRequest.SerializeToString,
                response_deserializer=prism__pb2.ModelCheckResponse.FromString,
                )
        self.ParsePropertiesString = channel.unary_unary(
                '/PrismProtoService/ParsePropertiesString',
                request_serializer=prism__pb2.ParsePropertiesStringRequest.SerializeToString,
                response_deserializer=prism__pb2.ParsePropertiesStringResponse.FromString,
                )
        self.ModelCheckString = channel.unary_unary(
                '/PrismProtoService/ModelCheckString',
                request_serializer=prism__pb2.ModelCheckStringRequest.SerializeToString,
                response_deserializer=prism__pb2.ModelCheckStringResponse.FromString,
                )
        self.ClosePrism = channel.unary_unary(
                '/PrismProtoService/ClosePrism',
                request_serializer=prism__pb2.ClosePrismRequest.SerializeToString,
                response_deserializer=prism__pb2.ClosePrismResponse.FromString,
                )


class PrismProtoServiceServicer(object):
    """Service Definitions
    """

    def UploadFile(self, request_iterator, context):
        """Generic method to upload files
        """
        context.set_code(grpc.StatusCode.UNIMPLEMENTED)
        context.set_details('Method not implemented!')
        raise NotImplementedError('Method not implemented!')

    def Initialise(self, request, context):
        """Initialise the PRISM engine
        """
        context.set_code(grpc.StatusCode.UNIMPLEMENTED)
        context.set_details('Method not implemented!')
        raise NotImplementedError('Method not implemented!')

    def ParseAndLoadModel(self, request, context):
        """Parse a model file
        """
        context.set_code(grpc.StatusCode.UNIMPLEMENTED)
        context.set_details('Method not implemented!')
        raise NotImplementedError('Method not implemented!')

    def LoadPRISMModel(self, request, context):
        """Load a PRISM model
        """
        context.set_code(grpc.StatusCode.UNIMPLEMENTED)
        context.set_details('Method not implemented!')
        raise NotImplementedError('Method not implemented!')

    def ParsePropertiesFile(self, request, context):
        """Parse a properties file
        """
        context.set_code(grpc.StatusCode.UNIMPLEMENTED)
        context.set_details('Method not implemented!')
        raise NotImplementedError('Method not implemented!')

    def DefineUndefinedConstants(self, request, context):
        """Define undefined constants
        """
        context.set_code(grpc.StatusCode.UNIMPLEMENTED)
        context.set_details('Method not implemented!')
        raise NotImplementedError('Method not implemented!')

    def ModelCheck(self, request, context):
        """Model check
        """
        context.set_code(grpc.StatusCode.UNIMPLEMENTED)
        context.set_details('Method not implemented!')
        raise NotImplementedError('Method not implemented!')

    def ModelCheckWithConstants(self, request, context):
        """Model check with constants
        """
        context.set_code(grpc.StatusCode.UNIMPLEMENTED)
        context.set_details('Method not implemented!')
        raise NotImplementedError('Method not implemented!')

    def ParsePropertiesString(self, request, context):
        """Parse a properties string
        """
        context.set_code(grpc.StatusCode.UNIMPLEMENTED)
        context.set_details('Method not implemented!')
        raise NotImplementedError('Method not implemented!')

    def ModelCheckString(self, request, context):
        """Model check a property specified as a string
        """
        context.set_code(grpc.StatusCode.UNIMPLEMENTED)
        context.set_details('Method not implemented!')
        raise NotImplementedError('Method not implemented!')

    def ClosePrism(self, request, context):
        """Close down PRISM
        """
        context.set_code(grpc.StatusCode.UNIMPLEMENTED)
        context.set_details('Method not implemented!')
        raise NotImplementedError('Method not implemented!')


def add_PrismProtoServiceServicer_to_server(servicer, server):
    rpc_method_handlers = {
            'UploadFile': grpc.stream_unary_rpc_method_handler(
                    servicer.UploadFile,
                    request_deserializer=prism__pb2.UploadRequest.FromString,
                    response_serializer=prism__pb2.UploadReply.SerializeToString,
            ),
            'Initialise': grpc.unary_unary_rpc_method_handler(
                    servicer.Initialise,
                    request_deserializer=prism__pb2.InitialiseRequest.FromString,
                    response_serializer=prism__pb2.InitialiseResponse.SerializeToString,
            ),
            'ParseAndLoadModel': grpc.unary_unary_rpc_method_handler(
                    servicer.ParseAndLoadModel,
                    request_deserializer=prism__pb2.ParseAndLoadModelRequest.FromString,
                    response_serializer=prism__pb2.ParseAndLoadModelReply.SerializeToString,
            ),
            'LoadPRISMModel': grpc.unary_unary_rpc_method_handler(
                    servicer.LoadPRISMModel,
                    request_deserializer=prism__pb2.LoadPRISMModelRequest.FromString,
                    response_serializer=prism__pb2.LoadPRISMModelResponse.SerializeToString,
            ),
            'ParsePropertiesFile': grpc.unary_unary_rpc_method_handler(
                    servicer.ParsePropertiesFile,
                    request_deserializer=prism__pb2.ParsePropertiesFileRequest.FromString,
                    response_serializer=prism__pb2.ParsePropertiesFileResponse.SerializeToString,
            ),
            'DefineUndefinedConstants': grpc.unary_unary_rpc_method_handler(
                    servicer.DefineUndefinedConstants,
                    request_deserializer=prism__pb2.DefineUndefinedConstantsRequest.FromString,
                    response_serializer=prism__pb2.DefineUndefinedConstantsResponse.SerializeToString,
            ),
            'ModelCheck': grpc.unary_unary_rpc_method_handler(
                    servicer.ModelCheck,
                    request_deserializer=prism__pb2.ModelCheckRequest.FromString,
                    response_serializer=prism__pb2.ModelCheckResponse.SerializeToString,
            ),
            'ModelCheckWithConstants': grpc.unary_unary_rpc_method_handler(
                    servicer.ModelCheckWithConstants,
                    request_deserializer=prism__pb2.ModelCheckWithConstantsRequest.FromString,
                    response_serializer=prism__pb2.ModelCheckResponse.SerializeToString,
            ),
            'ParsePropertiesString': grpc.unary_unary_rpc_method_handler(
                    servicer.ParsePropertiesString,
                    request_deserializer=prism__pb2.ParsePropertiesStringRequest.FromString,
                    response_serializer=prism__pb2.ParsePropertiesStringResponse.SerializeToString,
            ),
            'ModelCheckString': grpc.unary_unary_rpc_method_handler(
                    servicer.ModelCheckString,
                    request_deserializer=prism__pb2.ModelCheckStringRequest.FromString,
                    response_serializer=prism__pb2.ModelCheckStringResponse.SerializeToString,
            ),
            'ClosePrism': grpc.unary_unary_rpc_method_handler(
                    servicer.ClosePrism,
                    request_deserializer=prism__pb2.ClosePrismRequest.FromString,
                    response_serializer=prism__pb2.ClosePrismResponse.SerializeToString,
            ),
    }
    generic_handler = grpc.method_handlers_generic_handler(
            'PrismProtoService', rpc_method_handlers)
    server.add_generic_rpc_handlers((generic_handler,))


 # This class is part of an EXPERIMENTAL API.
class PrismProtoService(object):
    """Service Definitions
    """

    @staticmethod
    def UploadFile(request_iterator,
            target,
            options=(),
            channel_credentials=None,
            call_credentials=None,
            insecure=False,
            compression=None,
            wait_for_ready=None,
            timeout=None,
            metadata=None):
        return grpc.experimental.stream_unary(request_iterator, target, '/PrismProtoService/UploadFile',
            prism__pb2.UploadRequest.SerializeToString,
            prism__pb2.UploadReply.FromString,
            options, channel_credentials,
            insecure, call_credentials, compression, wait_for_ready, timeout, metadata)

    @staticmethod
    def Initialise(request,
            target,
            options=(),
            channel_credentials=None,
            call_credentials=None,
            insecure=False,
            compression=None,
            wait_for_ready=None,
            timeout=None,
            metadata=None):
        return grpc.experimental.unary_unary(request, target, '/PrismProtoService/Initialise',
            prism__pb2.InitialiseRequest.SerializeToString,
            prism__pb2.InitialiseResponse.FromString,
            options, channel_credentials,
            insecure, call_credentials, compression, wait_for_ready, timeout, metadata)

    @staticmethod
    def ParseAndLoadModel(request,
            target,
            options=(),
            channel_credentials=None,
            call_credentials=None,
            insecure=False,
            compression=None,
            wait_for_ready=None,
            timeout=None,
            metadata=None):
        return grpc.experimental.unary_unary(request, target, '/PrismProtoService/ParseAndLoadModel',
            prism__pb2.ParseAndLoadModelRequest.SerializeToString,
            prism__pb2.ParseAndLoadModelReply.FromString,
            options, channel_credentials,
            insecure, call_credentials, compression, wait_for_ready, timeout, metadata)

    @staticmethod
    def LoadPRISMModel(request,
            target,
            options=(),
            channel_credentials=None,
            call_credentials=None,
            insecure=False,
            compression=None,
            wait_for_ready=None,
            timeout=None,
            metadata=None):
        return grpc.experimental.unary_unary(request, target, '/PrismProtoService/LoadPRISMModel',
            prism__pb2.LoadPRISMModelRequest.SerializeToString,
            prism__pb2.LoadPRISMModelResponse.FromString,
            options, channel_credentials,
            insecure, call_credentials, compression, wait_for_ready, timeout, metadata)

    @staticmethod
    def ParsePropertiesFile(request,
            target,
            options=(),
            channel_credentials=None,
            call_credentials=None,
            insecure=False,
            compression=None,
            wait_for_ready=None,
            timeout=None,
            metadata=None):
        return grpc.experimental.unary_unary(request, target, '/PrismProtoService/ParsePropertiesFile',
            prism__pb2.ParsePropertiesFileRequest.SerializeToString,
            prism__pb2.ParsePropertiesFileResponse.FromString,
            options, channel_credentials,
            insecure, call_credentials, compression, wait_for_ready, timeout, metadata)

    @staticmethod
    def DefineUndefinedConstants(request,
            target,
            options=(),
            channel_credentials=None,
            call_credentials=None,
            insecure=False,
            compression=None,
            wait_for_ready=None,
            timeout=None,
            metadata=None):
        return grpc.experimental.unary_unary(request, target, '/PrismProtoService/DefineUndefinedConstants',
            prism__pb2.DefineUndefinedConstantsRequest.SerializeToString,
            prism__pb2.DefineUndefinedConstantsResponse.FromString,
            options, channel_credentials,
            insecure, call_credentials, compression, wait_for_ready, timeout, metadata)

    @staticmethod
    def ModelCheck(request,
            target,
            options=(),
            channel_credentials=None,
            call_credentials=None,
            insecure=False,
            compression=None,
            wait_for_ready=None,
            timeout=None,
            metadata=None):
        return grpc.experimental.unary_unary(request, target, '/PrismProtoService/ModelCheck',
            prism__pb2.ModelCheckRequest.SerializeToString,
            prism__pb2.ModelCheckResponse.FromString,
            options, channel_credentials,
            insecure, call_credentials, compression, wait_for_ready, timeout, metadata)

    @staticmethod
    def ModelCheckWithConstants(request,
            target,
            options=(),
            channel_credentials=None,
            call_credentials=None,
            insecure=False,
            compression=None,
            wait_for_ready=None,
            timeout=None,
            metadata=None):
        return grpc.experimental.unary_unary(request, target, '/PrismProtoService/ModelCheckWithConstants',
            prism__pb2.ModelCheckWithConstantsRequest.SerializeToString,
            prism__pb2.ModelCheckResponse.FromString,
            options, channel_credentials,
            insecure, call_credentials, compression, wait_for_ready, timeout, metadata)

    @staticmethod
    def ParsePropertiesString(request,
            target,
            options=(),
            channel_credentials=None,
            call_credentials=None,
            insecure=False,
            compression=None,
            wait_for_ready=None,
            timeout=None,
            metadata=None):
        return grpc.experimental.unary_unary(request, target, '/PrismProtoService/ParsePropertiesString',
            prism__pb2.ParsePropertiesStringRequest.SerializeToString,
            prism__pb2.ParsePropertiesStringResponse.FromString,
            options, channel_credentials,
            insecure, call_credentials, compression, wait_for_ready, timeout, metadata)

    @staticmethod
    def ModelCheckString(request,
            target,
            options=(),
            channel_credentials=None,
            call_credentials=None,
            insecure=False,
            compression=None,
            wait_for_ready=None,
            timeout=None,
            metadata=None):
        return grpc.experimental.unary_unary(request, target, '/PrismProtoService/ModelCheckString',
            prism__pb2.ModelCheckStringRequest.SerializeToString,
            prism__pb2.ModelCheckStringResponse.FromString,
            options, channel_credentials,
            insecure, call_credentials, compression, wait_for_ready, timeout, metadata)

    @staticmethod
    def ClosePrism(request,
            target,
            options=(),
            channel_credentials=None,
            call_credentials=None,
            insecure=False,
            compression=None,
            wait_for_ready=None,
            timeout=None,
            metadata=None):
        return grpc.experimental.unary_unary(request, target, '/PrismProtoService/ClosePrism',
            prism__pb2.ClosePrismRequest.SerializeToString,
            prism__pb2.ClosePrismResponse.FromString,
            options, channel_credentials,
            insecure, call_credentials, compression, wait_for_ready, timeout, metadata)
