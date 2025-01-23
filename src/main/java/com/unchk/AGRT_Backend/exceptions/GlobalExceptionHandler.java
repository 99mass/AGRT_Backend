package com.unchk.AGRT_Backend.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                "Accès refusé. Vous n'avez pas les permissions nécessaires pour cette action.",
                ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Authentification requise pour accéder à cette ressource.",
                ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }
    @ExceptionHandler(UserServiceException.class)
    public ResponseEntity<ErrorResponse> handleUserServiceException(UserServiceException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                ex.getStatus().value(),
                ex.getMessage(),
                ex.getClass().getSimpleName());
        return new ResponseEntity<>(errorResponse, ex.getStatus());
    }
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                ex.getClass().getSimpleName());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Une erreur interne est survenue. Veuillez réessayer plus tard.",
                ex.getClass().getSimpleName());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
// @RestControllerAdvice
// public class GlobalExceptionHandler {

//     @ExceptionHandler(ForbiddenException.class)
//     public ResponseEntity<Object> handleForbiddenException(ForbiddenException ex) {
//         Map<String, Object> response = new HashMap<>();
//         Map<String, String> errors = new HashMap<>();
        
//         errors.put("message", ex.getMessage());
//         response.put("status", HttpStatus.FORBIDDEN.value());
//         response.put("errors", errors);
        
//         return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
//     }

//     @ExceptionHandler(RuntimeException.class)
//     public ResponseEntity<Object> handleAllExceptions(RuntimeException ex) {
//         Map<String, Object> response = new HashMap<>();
//         Map<String, String> errors = new HashMap<>();

//         String errorCode = ex.getMessage();
//         String message = switch (errorCode) {
//             case "REQUIRED_FIRSTNAME" -> ErrorMessages.REQUIRED_FIRSTNAME.getMessage();
//             case "REQUIRED_LASTNAME" -> ErrorMessages.REQUIRED_LASTNAME.getMessage();
//             case "REQUIRED_EMAIL" -> ErrorMessages.REQUIRED_EMAIL.getMessage();
//             case "REQUIRED_PASSWORD" -> ErrorMessages.REQUIRED_PASSWORD.getMessage();
//             case "PASSWORD_TOO_SHORT" -> ErrorMessages.PASSWORD_TOO_SHORT.getMessage();
//             case "EMAIL_ALREADY_EXISTS" -> ErrorMessages.EMAIL_ALREADY_EXISTS.getMessage();
//             case "INVALID_EMAIL_FORMAT" -> ErrorMessages.INVALID_EMAIL_FORMAT.getMessage();
//             case "IMAGE_TOO_LARGE" -> ErrorMessages.IMAGE_TOO_LARGE.getMessage();
//             case "INVALID_IMAGE_FORMAT" -> ErrorMessages.INVALID_IMAGE_FORMAT.getMessage();
//             case "USER_NOT_FOUND" -> ErrorMessages.USER_NOT_FOUND.getMessage();
//             default -> ex.getMessage();
//         };

//         errors.put("message", message);
//         response.put("status", HttpStatus.BAD_REQUEST.value());
//         response.put("errors", errors);

//         return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
//     }

//     @SuppressWarnings("null")
//     @ExceptionHandler(MethodArgumentNotValidException.class)
//     public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException ex) {
//         Map<String, Object> response = new HashMap<>();
//         Map<String, String> errors = new HashMap<>();
//         System.out.println("MethodArgumentNotValidException");

//         ex.getBindingResult().getFieldErrors().forEach(error -> {
//             String field = error.getField();
//             String message = switch (field) {
//                 case "email" -> error.getCode().equals("NotBlank") ? ErrorMessages.REQUIRED_EMAIL.getMessage()
//                         : ErrorMessages.INVALID_EMAIL_FORMAT.getMessage();
//                 case "password" -> error.getCode().equals("NotBlank") ? ErrorMessages.REQUIRED_PASSWORD.getMessage()
//                         : ErrorMessages.PASSWORD_TOO_SHORT.getMessage();
//                 case "firstName" -> ErrorMessages.REQUIRED_FIRSTNAME.getMessage();
//                 case "lastName" -> ErrorMessages.REQUIRED_LASTNAME.getMessage();
//                 default -> error.getDefaultMessage();
//             };
//             errors.put(field, message);
//             System.out.println("Field: " + field + " Message: " + message);
//         });

//         response.put("status", HttpStatus.BAD_REQUEST.value());
//         response.put("errors", errors);

//         return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
//     }
// }