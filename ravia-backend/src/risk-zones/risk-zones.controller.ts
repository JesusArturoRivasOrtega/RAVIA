import { Controller, Get, Post, Patch, Delete, Param, Body, UseGuards } from '@nestjs/common';
import { ApiTags, ApiBearerAuth, ApiOperation } from '@nestjs/swagger';
import { RiskZonesService, CreateRiskZoneDto } from './risk-zones.service';
import { FirebaseAuthGuard } from '../common/guards/firebase-auth.guard';
import { RolesGuard } from '../common/guards/roles.guard';
import { CurrentUser } from '../common/decorators/current-user.decorator';
import { Roles, UserRole } from '../common/decorators/roles.decorator';
import { User } from '../users/user.entity';

@ApiTags('risk-zones')
@Controller({ path: 'risk-zones', version: '1' })
export class RiskZonesController {
  constructor(private readonly riskZonesService: RiskZonesService) {}

  @Get()
  @ApiOperation({ summary: 'Get all active risk zones' })
  findAll() {
    return this.riskZonesService.findAll();
  }

  @Get(':id')
  @ApiOperation({ summary: 'Get risk zone by ID' })
  findOne(@Param('id') id: string) {
    return this.riskZonesService.findById(id);
  }

  @Post()
  @ApiBearerAuth('firebase-token')
  @UseGuards(FirebaseAuthGuard, RolesGuard)
  @Roles(UserRole.MODERATOR, UserRole.ADMIN)
  @ApiOperation({ summary: 'Create risk zone (moderator+)' })
  create(@Body() dto: CreateRiskZoneDto, @CurrentUser() user: User) {
    return this.riskZonesService.create(dto, user);
  }

  @Patch(':id')
  @ApiBearerAuth('firebase-token')
  @UseGuards(FirebaseAuthGuard, RolesGuard)
  @Roles(UserRole.MODERATOR, UserRole.ADMIN)
  @ApiOperation({ summary: 'Update risk zone (moderator+)' })
  update(@Param('id') id: string, @Body() dto: Partial<CreateRiskZoneDto>) {
    return this.riskZonesService.update(id, dto);
  }

  @Delete(':id')
  @ApiBearerAuth('firebase-token')
  @UseGuards(FirebaseAuthGuard, RolesGuard)
  @Roles(UserRole.ADMIN)
  @ApiOperation({ summary: 'Deactivate risk zone (admin only)' })
  remove(@Param('id') id: string) {
    return this.riskZonesService.deactivate(id);
  }
}
