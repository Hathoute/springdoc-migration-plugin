package com.hathoute.springdocmigrationplugin;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiLiteralValue;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.PsiParameter;
import java.util.Arrays;
import java.util.stream.Collectors;

public final class PsiMigrationsHelper {

  public static final String SF_API_ANNOTATION = "io.swagger.annotations.Api";
  public static final String SF_APIOPERATION_ANNOTATION = "io.swagger.annotations.ApiOperation";
  public static final String SF_APIRESPONSES_ANNOTATION = "io.swagger.annotations.ApiResponses";
  public static final String SF_APIRESPONSE_ANNOTATION = "io.swagger.annotations.ApiResponse";
  public static final String SF_APIPARAM_ANNOTATION = "io.swagger.annotations.ApiParam";
  public static final String SF_APIMODEL_ANNOTATION = "io.swagger.annotations.ApiModel";
  public static final String SF_APIMODELPROPERTY_ANNOTATION = "io.swagger.annotations.ApiModelProperty";

  private static final String SD_TAG_ANNOTATION = "io.swagger.v3.oas.annotations.tags.Tag";
  private static final String SD_OPERATION_ANNOTATION = "io.swagger.v3.oas.annotations.Operation";
  private static final String SD_APIRESPONSE_ANNOTATION = "io.swagger.v3.oas.annotations.responses.ApiResponse";
  private static final String SD_APIRESPONSES_ANNOTATION = "io.swagger.v3.oas.annotations.responses.ApiResponses";
  private static final String SD_PARAMETER_ANNOTATION = "io.swagger.v3.oas.annotations.Parameter";
  private static final String SD_SCHEMA_ANNOTATION = "io.swagger.v3.oas.annotations.media.Schema";


  private PsiMigrationsHelper() {}

  public static boolean isEndpointMethod(final PsiMethod method) {
    return method.hasAnnotation(SF_APIOPERATION_ANNOTATION);
  }

  public static boolean isModelField(final PsiField field) {
    return field.hasAnnotation(SF_APIMODELPROPERTY_ANNOTATION);
  }

  public static void removeSpringfoxImports() {
    final var javaFile = file();
    final var importList = javaFile.getImportList();
    if (importList != null) {
      final var imports = importList.getImportStatements();
      for (final var importStatement : imports) {
        final var importText = importStatement.getText();
        if (importText.startsWith("import io.swagger.annotations.")) {
          importStatement.delete();
        }
      }
    }
  }

  public static boolean hasSpringfoxImports(final PsiJavaFile file) {
    return Arrays.stream(file.getImportList().getImportStatements())
        .map(PsiElement::getText)
        .anyMatch(s -> s.startsWith("import io.swagger.annotations."));
  }

  public static void migrateApiAnnotation(final PsiClass psiClass, final PsiAnnotation api) {
    final var diff = diff();
    final var apiValue = api.findDeclaredAttributeValue("value");

    diff.addSdAction(() -> {
      final var tag = addAnnotation(psiClass, SD_TAG_ANNOTATION);
      if (apiValue != null) {
        tag.setDeclaredAttributeValue("name", apiValue);
      }
    });

    diff.addSfAction(api::delete);
  }

  public static void migrateApiOperationAnnotation(final PsiMethod psiMethod, final PsiAnnotation apiOperation) {
    final var diff = diff();
    final var apiOperationValue = apiOperation.findDeclaredAttributeValue("value");
    final var apiOperationNotes = apiOperation.findDeclaredAttributeValue("notes");

    diff.addSdAction(() -> {
      final var operation = addAnnotation(psiMethod, SD_OPERATION_ANNOTATION);
      if (apiOperationValue != null) {
        operation.setDeclaredAttributeValue("summary", apiOperationValue);
      }
      if (apiOperationNotes != null) {
        operation.setDeclaredAttributeValue("description", apiOperationNotes);
      }
    });

    diff.addSfAction(apiOperation::delete);
  }

  public static void migrateApiResponseAnnotation(final PsiMethod psiMethod, final PsiAnnotation apiResponse) {
    final var diff = diff();

    diff.addSdAction(() -> {
      final var sdResponse = convertApiResponseAnnotation(apiResponse);
      psiMethod.getModifierList().add(sdResponse);
    });

    diff.addSfAction(apiResponse::delete);
  }

  public static void migrateApiResponsesAnnotation(final PsiMethod psiMethod, final PsiAnnotation apiResponses) {
    final var diff = diff();
    final var apiResponsesValue = apiResponses.findDeclaredAttributeValue("value");
    final var childrenAnnotations = Arrays.stream(apiResponsesValue.getChildren())
        .filter(PsiAnnotation.class::isInstance)
        .map(c -> (PsiAnnotation) c)
        .filter(c -> c.getQualifiedName().equals(SF_APIRESPONSE_ANNOTATION))
        .collect(Collectors.toList());

    diff.addSdAction(() -> {
      final var sdApiResponses = createAnnotation("@%s(value={})", SD_APIRESPONSES_ANNOTATION, psiMethod);
      final var responses = sdApiResponses.findDeclaredAttributeValue("value");
      final var rBrace = responses.getLastChild();
      for (final var apiResponse : childrenAnnotations) {
        final var sdResponse = convertApiResponseAnnotation(apiResponse);
        responses.addBefore(sdResponse, rBrace);
      }

      psiMethod.getModifierList().add(sdApiResponses);
    });

    diff.addSfAction(apiResponses::delete);
  }

  public static void migrateApiParam(final PsiAnnotation apiParam) {
    final var diff = diff();
    final var apiParamName = apiParam.findDeclaredAttributeValue("name");
    final var apiParamValue = apiParam.findDeclaredAttributeValue("value");
    final var apiParamRequired = apiParam.findDeclaredAttributeValue("required");
    final var apiParamExample = apiParam.findDeclaredAttributeValue("example");
    final var modifierList = (PsiModifierList) apiParam.getOwner();

    diff.addSdAction(() -> {
      final var parameter = addAnnotation(modifierList, SD_PARAMETER_ANNOTATION);
      if (apiParamName != null) {
        parameter.setDeclaredAttributeValue("name", apiParamName);
      }
      if (apiParamValue != null) {
        parameter.setDeclaredAttributeValue("description", apiParamValue);
      }
      if (apiParamRequired != null) {
        parameter.setDeclaredAttributeValue("required", apiParamRequired);
      }
      if (apiParamExample != null) {
        parameter.setDeclaredAttributeValue("example", apiParamExample);
      }
    });

    diff.addSfAction(apiParam::delete);
  }

  public static void migrateApiModel(final PsiClass psiClass, final PsiAnnotation apiModel) {
    final var diff = diff();
    final var apiModelValue = apiModel.findDeclaredAttributeValue("value");
    final var apiModelDescription = apiModel.findDeclaredAttributeValue("description");

    diff.addSdAction(() -> {
      final var schema = addAnnotation(psiClass, SD_SCHEMA_ANNOTATION);
      if (apiModelDescription != null && apiModelValue != null) {
        schema.setDeclaredAttributeValue("title", apiModelValue);
        schema.setDeclaredAttributeValue("description", apiModelDescription);
      }
      else if(apiModelValue != null || apiModelDescription != null) {
        schema.setDeclaredAttributeValue("description", apiModelValue != null ? apiModelValue : apiModelDescription);
      }
    });

    diff.addSfAction(apiModel::delete);

  }

  public static void migrateApiModelProperty(final PsiField psiField, final PsiAnnotation apiModelProperty) {
    final var diff = diff();
    final var apiModelPropertyValue = apiModelProperty.findDeclaredAttributeValue("value");
    final var apiModelPropertyExample = apiModelProperty.findDeclaredAttributeValue("example");
    final var apiModelPropertyRequired = apiModelProperty.findDeclaredAttributeValue("required");

    diff.addSdAction(() -> {
      final var parameter = addAnnotation(psiField, SD_SCHEMA_ANNOTATION);
      if (apiModelPropertyValue != null) {
        parameter.setDeclaredAttributeValue("description", apiModelPropertyValue);
      }
      if (apiModelPropertyExample != null) {
        parameter.setDeclaredAttributeValue("example", apiModelPropertyExample);
      }
      if (apiModelPropertyRequired instanceof PsiLiteralValue psiLiteralValue) {
        final var value = psiLiteralValue.getValue();
        if (value instanceof Boolean) {
          parameter.setDeclaredAttributeValue("requiredMode",
              createSchemaRequiredMode(psiField, (boolean) value));
        }
      }
    });

    diff.addSfAction(apiModelProperty::delete);
  }

  private static PsiExpression createSchemaRequiredMode(final PsiField field, final boolean required) {
    final var factory = PsiElementFactory.getInstance(field.getProject());
    final var shortName = addImport(SD_SCHEMA_ANNOTATION + ".RequiredMode");
    final var text = shortName + (required ? ".REQUIRED" : ".NOT_REQUIRED");
    return factory.createExpressionFromText(text, field);
  }

  private static PsiExpression createStringLiteral(final PsiElement element, final String value) {
    final var factory = PsiElementFactory.getInstance(element.getProject());
    return factory.createExpressionFromText("\"" + value + "\"", element);
  }

  private static PsiAnnotation convertApiResponseAnnotation(final PsiAnnotation apiResponse) {
    final var response = createAnnotation("@%s", SD_APIRESPONSE_ANNOTATION,
        apiResponse.getParent());

    final var apiResponseCode = apiResponse.findDeclaredAttributeValue("code");
    final var apiResponseMessage = apiResponse.findDeclaredAttributeValue("message");
    final var apiResponseExample = apiResponse.findDeclaredAttributeValue("response");

    if (apiResponseCode == null || apiResponseMessage == null) {
      return response;
    }

    if (apiResponseCode instanceof PsiLiteralValue psiLiteralValue) {
      final var value = psiLiteralValue.getValue();
      response.setDeclaredAttributeValue("responseCode",
          createStringLiteral(response, String.valueOf(value)));
    } else {
      response.setDeclaredAttributeValue("responseCode", apiResponseCode);
    }

    response.setDeclaredAttributeValue("description", apiResponseMessage);
    if (apiResponseExample != null) {
      response.setDeclaredAttributeValue("content", apiResponseExample);
    }

    return response;
  }

  private static PsiAnnotation addAnnotation(final PsiElement element, final String qualifiedName) {
    final PsiModifierList owner;
    if (element instanceof PsiClass psiClass) {
      owner = psiClass.getModifierList();
    } else if (element instanceof PsiMethod psiMethod) {
      owner = psiMethod.getModifierList();
    } else if (element instanceof PsiField psiField) {
      owner = psiField.getModifierList();
    } else if (element instanceof PsiParameter psiParameter) {
      owner = psiParameter.getModifierList();
    } else {
      throw new IllegalArgumentException("Unsupported element type: " + element.getClass());
    }

    // Owner is never null, because we are migrating from Springfox annotations
    // => the element has already been annotated
    // => element has modifiers
    assert owner != null;

    return addAnnotation(owner, qualifiedName);
  }

  private static PsiAnnotation addAnnotation(final PsiModifierList owner, final String qualifiedName) {
    final var shortName = addImport(qualifiedName);
    return owner.addAnnotation(shortName);
  }

  private static PsiAnnotation createAnnotation(final String format, final String qualifiedName, final PsiElement context) {
    final var shortName = addImport(qualifiedName);
    return PsiElementFactory.getInstance(context.getProject()).createAnnotationFromText(
        String.format(format, shortName), context);
  }

  private static String addImport(final String fullName) {
    final var file = file();
      final var importedClass = PsiClassFinder.findClassEverywhere(file.getProject(), fullName);
      if(file.importClass(importedClass)) {
        final var splitted = fullName.split("\\.");
        return splitted[splitted.length - 1];
    }

    return fullName;
  }

  private static PsiFileDiff diff() {
    final var fileProcessor = PsiContext.getInstance().currentProcessor();
    return fileProcessor.getDiff();
  }

  private static PsiJavaFile file() {
    final var fileProcessor = PsiContext.getInstance().currentProcessor();
    return fileProcessor.getFile();
  }

}
