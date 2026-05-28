import { Controller, Get, Post, Patch, Delete, Param, Body, Query, UseGuards } from '@nestjs/common';
import { ApiTags, ApiBearerAuth, ApiOperation } from '@nestjs/swagger';
import { MissingPersonsService } from './missing-persons.service';
import { FirebaseAuthGuard } from '../common/guards/firebase-auth.guard';
import { RolesGuard } from '../common/guards/roles.guard';
import { CurrentUser } from '../common/decorators/current-user.decorator';
import { Roles, UserRole } from '../common/decorators/roles.decorator';
import { User } from '../users/user.entity';
import {
  CreateMissingPersonDto,
  ReportSightingDto,
  UpdateMissingPersonDto,
  UpdateMissingPersonStatusDto,
} from './dto/missing-person.dto';
import { MissingPersonStatus } from './missing-person.entity';

@ApiTags('missing-persons')
@Controller({ path: 'missing-persons', version: '1' })
export class MissingPersonsController {
  constructor(private readonly service: MissingPersonsService) {}

  @Get()
  @ApiOperation({ summary: 'Get active missing persons reports' })
  findAll() {
    return this.service.findAll();
  }

  @Get('review')
  @ApiBearerAuth('firebase-token')
  @UseGuards(FirebaseAuthGuard, RolesGuard)
  @Roles(UserRole.MODERATOR, UserRole.ADMIN)
  @ApiOperation({ summary: 'Get missing person reports pending moderator review' })
  findForReview() {
    return this.service.findForReview();
  }

  @Get('all')
  @ApiBearerAuth('firebase-token')
  @UseGuards(FirebaseAuthGuard, RolesGuard)
  @Roles(UserRole.MODERATOR, UserRole.ADMIN)
  @ApiOperation({ summary: 'Get all missing person reports for moderation/admin' })
  findAllForAdmin(@Query('status') status?: MissingPersonStatus, @Query('limit') limit?: number) {
    return this.service.findAllForAdmin(status, Number(limit ?? 100));
  }

  @Get(':id')
  @ApiOperation({ summary: 'Get missing person report by ID' })
  findOne(@Param('id') id: string) {
    return this.service.findById(id);
  }

  @Post()
  @ApiBearerAuth('firebase-token')
  @UseGuards(FirebaseAuthGuard)
  @ApiOperation({ summary: 'Create missing person report' })
  create(@Body() dto: CreateMissingPersonDto, @CurrentUser() user: User) {
    return this.service.create(dto, user);
  }

  @Post(':id/sightings')
  @ApiBearerAuth('firebase-token')
  @UseGuards(FirebaseAuthGuard)
  @ApiOperation({ summary: 'Report a sighting' })
  reportSighting(
    @Param('id') id: string,
    @Body() dto: ReportSightingDto,
    @CurrentUser() user: User,
  ) {
    return this.service.reportSighting(id, dto, user);
  }

  @Patch(':id/status')
  @ApiBearerAuth('firebase-token')
  @UseGuards(FirebaseAuthGuard, RolesGuard)
  @Roles(UserRole.MODERATOR, UserRole.ADMIN)
  @ApiOperation({ summary: 'Update status (moderator+)' })
  updateStatus(@Param('id') id: string, @Body() dto: UpdateMissingPersonStatusDto) {
    return this.service.updateStatus(id, dto.status);
  }

  @Patch(':id')
  @ApiBearerAuth('firebase-token')
  @UseGuards(FirebaseAuthGuard, RolesGuard)
  @Roles(UserRole.MODERATOR, UserRole.ADMIN)
  @ApiOperation({ summary: 'Update missing person report details (moderator+)' })
  update(@Param('id') id: string, @Body() dto: UpdateMissingPersonDto) {
    return this.service.update(id, dto);
  }

  @Delete(':id')
  @ApiBearerAuth('firebase-token')
  @UseGuards(FirebaseAuthGuard, RolesGuard)
  @Roles(UserRole.ADMIN)
  @ApiOperation({ summary: 'Delete missing person report (admin only)' })
  remove(@Param('id') id: string) {
    return this.service.delete(id);
  }
}
