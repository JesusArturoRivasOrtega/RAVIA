import { Controller, Get, Post, Patch, Param, Body, UseGuards } from '@nestjs/common';
import { ApiTags, ApiBearerAuth, ApiOperation } from '@nestjs/swagger';
import { AlertsService, CreateAlertDto } from './alerts.service';
import { FirebaseAuthGuard } from '../common/guards/firebase-auth.guard';
import { RolesGuard } from '../common/guards/roles.guard';
import { CurrentUser } from '../common/decorators/current-user.decorator';
import { Roles, UserRole } from '../common/decorators/roles.decorator';
import { User } from '../users/user.entity';

@ApiTags('alerts')
@Controller({ path: 'alerts', version: '1' })
export class AlertsController {
  constructor(private readonly alertsService: AlertsService) {}

  @Get()
  @ApiOperation({ summary: 'Get active alerts' })
  findActive() {
    return this.alertsService.findActive();
  }

  @Get('all')
  @ApiBearerAuth('firebase-token')
  @UseGuards(FirebaseAuthGuard, RolesGuard)
  @Roles(UserRole.MODERATOR, UserRole.ADMIN)
  @ApiOperation({ summary: 'Get all alerts including inactive (moderator+)' })
  findAll() {
    return this.alertsService.findAll();
  }

  @Get(':id')
  @ApiOperation({ summary: 'Get alert by ID' })
  findOne(@Param('id') id: string) {
    return this.alertsService.findById(id);
  }

  @Post()
  @ApiBearerAuth('firebase-token')
  @UseGuards(FirebaseAuthGuard, RolesGuard)
  @Roles(UserRole.MODERATOR, UserRole.ADMIN)
  @ApiOperation({ summary: 'Create alert (moderator+)' })
  create(@Body() dto: CreateAlertDto, @CurrentUser() user: User) {
    return this.alertsService.create(dto, user);
  }

  @Patch(':id/deactivate')
  @ApiBearerAuth('firebase-token')
  @UseGuards(FirebaseAuthGuard, RolesGuard)
  @Roles(UserRole.MODERATOR, UserRole.ADMIN)
  @ApiOperation({ summary: 'Deactivate alert (moderator+)' })
  deactivate(@Param('id') id: string) {
    return this.alertsService.deactivate(id);
  }
}
