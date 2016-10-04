package com.github.tminglei.swagger;

import com.github.tminglei.bind.Framework;
import io.swagger.models.*;
import io.swagger.models.auth.ApiKeyAuthDefinition;
import io.swagger.models.auth.BasicAuthDefinition;
import io.swagger.models.auth.In;
import io.swagger.models.auth.OAuth2Definition;
import io.swagger.models.properties.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.github.tminglei.swagger.SwaggerUtils.*;

/**
 * Context class to hold swagger instance and related helper methods
 */
public class SwaggerContext {
    private static final Logger logger = LoggerFactory.getLogger(SwaggerContext.class);

    private static Swagger swagger = new Swagger();

    public static Swagger swagger() {
        return swagger;
    }

    public static SharingHolder share() {
        return new SharingHolder();
    }

    public static ExOperation operation(String method, String path) {
        return operation(method, path, null);
    }
    public static ExOperation operation(String method, String path, SharingHolder sharing) {
        checkNotEmpty(path, "'path' CAN'T be null or empty!!!");
        checkNotEmpty(method, "'method' CAN'T be null or empty!!!");

        sharing = sharing != null ? sharing : new SharingHolder();
        path = joinPaths(sharing.commonPath(), path);
        HttpMethod httpMethod = HttpMethod.valueOf(method.toUpperCase());
        synchronized (swagger) {
            if (swagger.getPath(path) == null) {
                logger.info(">>> adding path - '" + path + "'");
                swagger.path(path, new Path());
            }

            Path pathObj = swagger.getPath(path);
            if (pathObj.getOperationMap().get(httpMethod) != null) {
                throw new IllegalArgumentException("DUPLICATED operation - " + httpMethod + " '" + path + "'");
            }

            logger.info(">>> adding operation - " + httpMethod + " '" + path + "'");
            pathObj.set(method.toLowerCase(), new ExOperation().merge(sharing));
            return (ExOperation) pathObj.getOperationMap().get(httpMethod);
        }
    }

    public static MParamBuilder param(Framework.Mapping<?> mapping) {
        return new MParamBuilder(mapping);
    }
    public static Model model(Framework.Mapping<?> mapping) {
        scanRegisterNamedModels(mapping);
        return mHelper.mToModel(mapping);
    }
    public static Response response(Framework.Mapping<?> mapping) {
        scanRegisterNamedModels(mapping);
        return mHelper.mToResponse(mapping);
    }
    public static Response response() {
        return new Response();
    }
    public static Property prop(Framework.Mapping<?> mapping) {
        scanRegisterNamedModels(mapping);
        return mHelper.mToProperty(mapping);
    }

    public static void scanRegisterNamedModels(Framework.Mapping<?> mapping) {
        synchronized (swagger) {
            mHelper.scanModels(mapping).forEach(p -> {
                Model existed = swagger.getDefinitions() == null ? null : swagger.getDefinitions().get(p.getKey());
                if (existed == null) swagger.model(p.getKey(), p.getValue());
                else if (!existed.equals(p.getValue())) {
                    throw new IllegalArgumentException("CONFLICTED model definitions for '" + p.getKey() + "'!!!");
                }
            });
        }
    }

    public static Info info() {
        return new Info();
    }
    public static Tag tag(String name) {
        return new Tag().name(name);
    }
    public static Contact contact() {
        return new Contact();
    }
    public static License license() {
        return new License();
    }
    public static BasicAuthDefinition basicAuth() {
        return new BasicAuthDefinition();
    }
    public static ApiKeyAuthDefinition apiKeyAuth(String name, In in) {
        return new ApiKeyAuthDefinition(name, in);
    }
    public static OAuth2Definition oAuth2() {
        return new OAuth2Definition();
    }
    public static ExternalDocs externalDocs() {
        return new ExternalDocs();
    }

    ///////////////////////////////////////////////////////////////////////

    static MSwaggerHelper mHelper = new MSwaggerHelper(); // extend and replace it when necessary
    public static void setMHelper(MSwaggerHelper mHelper) {
        SwaggerContext.mHelper = mHelper;
    }
}
