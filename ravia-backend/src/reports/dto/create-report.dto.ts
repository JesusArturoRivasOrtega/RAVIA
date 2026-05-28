import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import {
  ArrayMaxSize,
  IsBoolean,
  IsArray,
  IsEnum,
  IsNumber,
  IsOptional,
  IsString,
  Max,
  MaxLength,
  Min,
  MinLength,
  ValidateNested,
} from 'class-validator';
import { Type } from 'class-transformer';
import { ReportCategory, ReportPriority } from '../report.entity';

enum ReportMediaType {
  IMAGE = 'image',
  VIDEO = 'video',
  AUDIO = 'audio',
}

class ReportMediaDto {
  @IsString()
  @MaxLength(80)
  id: string;

  @IsString()
  @MaxLength(800000)
  url: string;

  @IsEnum(ReportMediaType)
  type: ReportMediaType;

  @IsOptional()
  @IsString()
  @MaxLength(800000)
  thumbnailUrl?: string;
}

export class CreateReportDto {
  @ApiProperty()
  @IsString()
  @MinLength(5)
  @MaxLength(100)
  title: string;

  @ApiProperty()
  @IsString()
  @MinLength(10)
  @MaxLength(2000)
  description: string;

  @ApiProperty({ enum: ReportCategory })
  @IsEnum(ReportCategory)
  category: ReportCategory;

  @ApiPropertyOptional({ enum: ReportPriority, default: ReportPriority.MEDIUM })
  @IsOptional()
  @IsEnum(ReportPriority)
  priority?: ReportPriority;

  @ApiProperty()
  @IsNumber()
  @Min(-90)
  @Max(90)
  lat: number;

  @ApiProperty()
  @IsNumber()
  @Min(-180)
  @Max(180)
  lng: number;

  @ApiPropertyOptional({ description: 'Street, address, or local reference for the incident location' })
  @IsOptional()
  @IsString()
  @MaxLength(240)
  address?: string;

  @ApiPropertyOptional({ default: false })
  @IsOptional()
  @IsBoolean()
  isAnonymous?: boolean;

  @ApiPropertyOptional({ description: 'Run AI analysis on submission' })
  @IsOptional()
  @IsBoolean()
  requestAiAnalysis?: boolean;

  @ApiPropertyOptional({ type: [ReportMediaDto] })
  @IsOptional()
  @IsArray()
  @ArrayMaxSize(8)
  @ValidateNested({ each: true })
  @Type(() => ReportMediaDto)
  media?: ReportMediaDto[];
}
