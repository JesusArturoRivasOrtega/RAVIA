import {
  ExceptionFilter,
  Catch,
  ArgumentsHost,
  HttpException,
  HttpStatus,
  Logger,
} from '@nestjs/common';
import { Request, Response } from 'express';

type ErrorPayload = string | {
  message?: string | string[];
  code?: string;
  details?: unknown;
  error?: string;
};

@Catch()
export class GlobalExceptionFilter implements ExceptionFilter {
  private readonly logger = new Logger(GlobalExceptionFilter.name);

  catch(exception: unknown, host: ArgumentsHost) {
    const ctx = host.switchToHttp();
    const response = ctx.getResponse<Response>();
    const request = ctx.getRequest<Request>();

    const status =
      exception instanceof HttpException
        ? exception.getStatus()
        : HttpStatus.INTERNAL_SERVER_ERROR;

    const payload: ErrorPayload =
      exception instanceof HttpException
        ? exception.getResponse()
        : 'Internal server error';
    const requestId = request.headers['x-request-id']?.toString();

    const code =
      typeof payload === 'object' && payload.code
        ? payload.code
        : status >= 500
          ? 'INTERNAL_ERROR'
          : 'REQUEST_ERROR';
    const message =
      typeof payload === 'object'
        ? payload.message ?? payload.error ?? 'Unexpected error'
        : payload;

    if (status >= 500) {
      this.logger.error(
        `${request.method} ${request.url} requestId=${requestId ?? 'none'}`,
        exception instanceof Error ? exception.stack : String(exception),
      );
    }

    response.status(status).json({
      statusCode: status,
      code,
      timestamp: new Date().toISOString(),
      path: request.url,
      requestId,
      message,
      details: typeof payload === 'object' ? payload.details : undefined,
    });
  }
}
