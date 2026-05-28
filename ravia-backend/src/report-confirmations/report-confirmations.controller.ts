import { Controller, Post, Get, Param, Body, UseGuards } from '@nestjs/common';
import { ApiTags, ApiBearerAuth, ApiOperation } from '@nestjs/swagger';
import { IsEnum, IsOptional, IsString } from 'class-validator';
import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { ReportConfirmationsService, ConfirmationType } from './report-confirmations.service';
import { FirebaseAuthGuard } from '../common/guards/firebase-auth.guard';
import { CurrentUser } from '../common/decorators/current-user.decorator';
import { User } from '../users/user.entity';

class CreateConfirmationDto {
  @ApiProperty({ enum: ConfirmationType })
  @IsEnum(ConfirmationType)
  type: ConfirmationType;

  @ApiPropertyOptional()
  @IsOptional()
  @IsString()
  comment?: string;
}

@ApiTags('report-confirmations')
@ApiBearerAuth('firebase-token')
@UseGuards(FirebaseAuthGuard)
@Controller({ path: 'reports/:reportId/confirmations', version: '1' })
export class ReportConfirmationsController {
  constructor(private readonly service: ReportConfirmationsService) {}

  @Post()
  @ApiOperation({ summary: 'Confirm or flag a report' })
  confirm(
    @Param('reportId') reportId: string,
    @Body() dto: CreateConfirmationDto,
    @CurrentUser() user: User,
  ) {
    return this.service.confirm(reportId, dto.type, user, dto.comment);
  }

  @Get()
  @ApiOperation({ summary: 'Get all confirmations for a report' })
  getForReport(@Param('reportId') reportId: string) {
    return this.service.getForReport(reportId);
  }
}
