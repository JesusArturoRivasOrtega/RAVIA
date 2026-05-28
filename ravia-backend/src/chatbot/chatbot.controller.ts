import { Controller, Post, Get, Body } from '@nestjs/common';
import { ApiTags, ApiOperation } from '@nestjs/swagger';
import { IsOptional, IsString } from 'class-validator';
import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { ChatbotService } from './chatbot.service';

class ChatRequestDto {
  @ApiProperty()
  @IsString()
  message: string;

  @ApiPropertyOptional()
  @IsOptional()
  @IsString()
  sessionId?: string;
}

@ApiTags('chatbot')
@Controller({ path: 'chatbot', version: '1' })
export class ChatbotController {
  constructor(private readonly chatbotService: ChatbotService) {}

  @Post('message')
  @ApiOperation({ summary: 'Send a message to the RAVIA assistant' })
  chat(@Body() dto: ChatRequestDto) {
    return this.chatbotService.chat(dto);
  }

  @Get('suggestions')
  @ApiOperation({ summary: 'Get quick suggestion topics' })
  getSuggestions() {
    return this.chatbotService.getSuggestions();
  }
}
