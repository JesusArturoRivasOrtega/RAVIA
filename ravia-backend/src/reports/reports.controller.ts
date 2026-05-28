import {
  Controller, Get, Post, Patch, Delete, Param, Body, Query, UseGuards
} from '@nestjs/common';
import { ApiTags, ApiBearerAuth, ApiOperation } from '@nestjs/swagger';
import { ReportsService } from './reports.service';
import { CreateReportDto } from './dto/create-report.dto';
import { UpdateReportDto, UpdateReportStatusDto, QueryReportsDto } from './dto/update-report.dto';
import { FirebaseAuthGuard } from '../common/guards/firebase-auth.guard';
import { RolesGuard } from '../common/guards/roles.guard';
import { CurrentUser } from '../common/decorators/current-user.decorator';
import { Roles, UserRole } from '../common/decorators/roles.decorator';
import { User } from '../users/user.entity';

@ApiTags('reports')
@ApiBearerAuth('firebase-token')
@UseGuards(FirebaseAuthGuard, RolesGuard)
@Controller({ path: 'reports', version: '1' })
export class ReportsController {
  constructor(private readonly reportsService: ReportsService) {}

  @Post()
  @ApiOperation({ summary: 'Create a new report' })
  create(@Body() dto: CreateReportDto, @CurrentUser() user: User) {
    return this.reportsService.create(dto, user);
  }

  @Get()
  @ApiOperation({ summary: 'List reports with optional filters' })
  findAll(@Query() query: QueryReportsDto) {
    return this.reportsService.findAll(query);
  }

  @Get('my')
  @ApiOperation({ summary: 'Get current user reports' })
  findMine(@CurrentUser() user: User, @Query('limit') limit?: number) {
    return this.reportsService.findByUser(user.id, limit);
  }

  @Get(':id')
  @ApiOperation({ summary: 'Get report by ID' })
  findOne(@Param('id') id: string) {
    return this.reportsService.findById(id);
  }

  @Patch(':id')
  @ApiOperation({ summary: 'Update report content' })
  update(@Param('id') id: string, @Body() dto: UpdateReportDto, @CurrentUser() user: User) {
    return this.reportsService.update(id, dto, user);
  }

  @Patch(':id/status')
  @Roles(UserRole.MODERATOR, UserRole.ADMIN)
  @ApiOperation({ summary: 'Update report status (moderator+)' })
  updateStatus(@Param('id') id: string, @Body() dto: UpdateReportStatusDto, @CurrentUser() user: User) {
    return this.reportsService.updateStatus(id, dto, user);
  }

  @Post(':id/analyze')
  @ApiOperation({ summary: 'Run AI analysis on a report' })
  analyze(@Param('id') id: string) {
    return this.reportsService.analyzeWithAi(id);
  }

  @Delete(':id')
  @ApiOperation({ summary: 'Delete a report' })
  remove(@Param('id') id: string, @CurrentUser() user: User) {
    return this.reportsService.delete(id, user);
  }
}
