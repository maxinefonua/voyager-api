//package org.voyager.error;
//
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.ControllerAdvice;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//
//@ControllerAdvice
//public class GlobalExceptionHandler {
//
//    // Handle NullPointerException globally
//    @ExceptionHandler(NullPointerException.class)
//    public ResponseEntity<String> handleNullPointerException(NullPointerException ex) {
//        return ResponseEntity
//                .status(HttpStatus.BAD_REQUEST)
//                .body("Error: Null value encountered. " + ex.getMessage());
//    }
//
//    // Optional: Handle other exceptions generically
////    @ExceptionHandler(Exception.class)
////    public ResponseEntity<String> handleGeneralException(Exception ex) {
////        return ResponseEntity
////                .status(HttpStatus.INTERNAL_SERVER_ERROR)
////                .body("Internal Server Error: " + ex.getMessage());
////    }
//}