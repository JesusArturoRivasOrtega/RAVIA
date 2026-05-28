import { Controller, Post, Body, UseGuards, Get } from '@nestjs/common';
import { ApiTags, ApiBearerAuth, ApiOperation } from '@nestjs/swagger';
import { AuthService } from './auth.service';
import { RegisterDto } from './dto/auth.dto';
import { FirebaseAuthGuard } from '../common/guards/firebase-auth.guard';
import { CurrentUser } from '../common/decorators/current-user.decorator';
import { User } from '../users/user.entity';
import { IsEmail } from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';

class ResetPasswordDto {
  @ApiProperty()
  @IsEmail()
  email: string;
}

@ApiTags('auth')
@Controller({ path: 'auth', version: '1' })
export class AuthController {
  constructor(private readonly authService: AuthService) {}

  @Post('register')
  @ApiOperation({ summary: 'Register new user (creates Firebase Auth + Firestore profile)' })
  register(@Body() dto: RegisterDto) {
    return this.authService.register(dto);
  }

  @Post('reset-password')
  @ApiOperation({ summary: 'Send password reset email' })
  resetPassword(@Body() dto: ResetPasswordDto) {
    return this.authService.resetPassword(dto.email);
  }

  @Get('me')
  @ApiBearerAuth('firebase-token')
  @UseGuards(FirebaseAuthGuard)
  @ApiOperation({ summary: 'Get current user from token' })
  getMe(@CurrentUser() user: User) {
    return user;
  }
}
