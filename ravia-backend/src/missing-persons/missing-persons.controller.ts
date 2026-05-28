import { Controller, Get, Post, Patch, Param, Body, UseGuards } from '@nestjs/common';
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
  UpdateMissingPersonStatusDto,
} from './dto/missing-person.dto';

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
}
