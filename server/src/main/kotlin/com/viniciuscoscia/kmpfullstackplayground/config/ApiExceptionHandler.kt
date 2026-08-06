package com.viniciuscoscia.kmpfullstackplayground.config

import com.viniciuscoscia.kmpfullstackplayground.catalog.CatalogNotFoundException
import com.viniciuscoscia.kmpfullstackplayground.research.ResearchNotFoundException
import com.viniciuscoscia.kmpfullstackplayground.reporting.ReportUnavailableException
import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.ServletWebRequest
import java.net.URI

@RestControllerAdvice
class ApiExceptionHandler {
    @ExceptionHandler(CatalogNotFoundException::class, ResearchNotFoundException::class)
    fun notFound(error: NoSuchElementException, request: ServletWebRequest): ProblemDetail =
        problem(HttpStatus.NOT_FOUND, "Resource not found", error.message, request)

    @ExceptionHandler(
        IllegalArgumentException::class,
        ConstraintViolationException::class,
        MethodArgumentNotValidException::class,
    )
    fun invalid(error: Exception, request: ServletWebRequest): ProblemDetail =
        problem(HttpStatus.BAD_REQUEST, "Invalid request", error.message, request)

    @ExceptionHandler(ReportUnavailableException::class)
    fun unavailable(error: ReportUnavailableException, request: ServletWebRequest): ProblemDetail =
        problem(HttpStatus.SERVICE_UNAVAILABLE, "Analytics template unavailable", error.message, request)

    private fun problem(
        status: HttpStatus,
        title: String,
        detail: String?,
        request: ServletWebRequest,
    ): ProblemDetail = ProblemDetail.forStatusAndDetail(status, detail ?: title).also {
        it.title = title
        it.type = URI.create("https://substance-atlas.local/problems/${status.value()}")
        it.instance = URI.create(request.request.requestURI)
    }
}
