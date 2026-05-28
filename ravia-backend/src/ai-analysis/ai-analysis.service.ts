import { Injectable, Logger } from '@nestjs/common';
import { ReportCategory, ReportPriority } from '../reports/report.entity';

interface AnalysisResult {
  suggestedCategory: ReportCategory;
  suggestedPriority: ReportPriority;
  confidence: number;
  summary: string;
  missingInfo: string[];
  possibleDuplicate: boolean;
  duplicateReportId?: string;
  analyzedAt: Date;
}

interface CategoryRule {
  keywords: string[];
  category: ReportCategory;
  priority: ReportPriority;
  confidence: number;
}

const CATEGORY_RULES: CategoryRule[] = [
  {
    keywords: ['incendio', 'fuego', 'llamas', 'quemando', 'humo', 'fire'],
    category: ReportCategory.FIRE,
    priority: ReportPriority.CRITICAL,
    confidence: 0.92,
  },
  {
    keywords: ['accidente', 'choque', 'colisión', 'atropellado', 'volcadura', 'crash'],
    category: ReportCategory.ACCIDENT,
    priority: ReportPriority.HIGH,
    confidence: 0.88,
  },
  {
    keywords: ['herido', 'herida', 'inconsciente', 'desmayado', 'sangre', 'injured', 'medical'],
    category: ReportCategory.MEDICAL,
    priority: ReportPriority.CRITICAL,
    confidence: 0.85,
  },
  {
    keywords: ['inundación', 'inundado', 'agua', 'desbordamiento', 'flood'],
    category: ReportCategory.FLOOD,
    priority: ReportPriority.HIGH,
    confidence: 0.87,
  },
  {
    keywords: ['robo', 'asalto', 'ladrón', 'robaron', 'theft', 'steal'],
    category: ReportCategory.THEFT,
    priority: ReportPriority.HIGH,
    confidence: 0.83,
  },
  {
    keywords: ['agresión', 'golpes', 'pelea', 'violencia', 'assault', 'attack'],
    category: ReportCategory.ASSAULT,
    priority: ReportPriority.HIGH,
    confidence: 0.82,
  },
  {
    keywords: ['desaparecido', 'desapareció', 'missing', 'no aparece', 'buscando'],
    category: ReportCategory.MISSING_PERSON,
    priority: ReportPriority.HIGH,
    confidence: 0.88,
  },
  {
    keywords: ['gas', 'fuga', 'olor a gas', 'gas leak'],
    category: ReportCategory.GAS_LEAK,
    priority: ReportPriority.CRITICAL,
    confidence: 0.91,
  },
  {
    keywords: ['bache', 'poste', 'alumbrado', 'cable', 'fuga de agua', 'infrastructure'],
    category: ReportCategory.INFRASTRUCTURE,
    priority: ReportPriority.LOW,
    confidence: 0.78,
  },
  {
    keywords: ['sospechoso', 'extraño', 'merodeando', 'suspicious'],
    category: ReportCategory.SUSPICIOUS,
    priority: ReportPriority.MEDIUM,
    confidence: 0.70,
  },
];

@Injectable()
export class AiAnalysisService {
  private readonly logger = new Logger(AiAnalysisService.name);

  async analyze(
    title: string,
    description: string,
    hintCategory?: ReportCategory,
  ): Promise<AnalysisResult> {
    const provider = process.env.AI_PROVIDER ?? 'internal';

    if (provider === 'openai' && process.env.OPENAI_API_KEY) {
      return this.analyzeWithOpenAI(title, description);
    }

    return this.analyzeInternal(title, description, hintCategory);
  }

  private analyzeInternal(
    title: string,
    description: string,
    hintCategory?: ReportCategory,
  ): AnalysisResult {
    const text = `${title} ${description}`.toLowerCase();

    let best: CategoryRule | null = null;
    let bestScore = 0;

    for (const rule of CATEGORY_RULES) {
      const matches = rule.keywords.filter((kw) => text.includes(kw));
      if (matches.length > bestScore) {
        bestScore = matches.length;
        best = rule;
      }
    }

    const suggestedCategory = best?.category ?? hintCategory ?? ReportCategory.OTHER;
    const suggestedPriority = best?.priority ?? ReportPriority.MEDIUM;
    const confidence = best ? Math.min(best.confidence + (bestScore - 1) * 0.02, 0.99) : 0.5;

    const missingInfo: string[] = [];
    if (description.length < 50) missingInfo.push('Descripción muy corta — incluye más detalles');
    if (!text.match(/calle|avenida|colonia|entre|frente|cerca|blvd|blvd\.|av\./)) {
      missingInfo.push('Especifica la dirección o referencias de ubicación');
    }
    if (suggestedCategory === ReportCategory.MEDICAL && !text.includes('cuántas')) {
      missingInfo.push('¿Cuántas personas están involucradas?');
    }

    const summary = this.buildSummary(suggestedCategory, suggestedPriority, confidence, description);

    this.logger.debug(`AI analysis: category=${suggestedCategory} confidence=${confidence}`);

    return {
      suggestedCategory,
      suggestedPriority,
      confidence,
      summary,
      missingInfo,
      possibleDuplicate: false,
      analyzedAt: new Date(),
    };
  }

  private buildSummary(
    category: ReportCategory,
    priority: ReportPriority,
    confidence: number,
    description: string,
  ): string {
    const excerpt = description.length > 120 ? description.slice(0, 120) + '…' : description;
    const confidencePct = Math.round(confidence * 100);
    return `Incidente clasificado como ${category.replace('_', ' ')} con prioridad ${priority}. Confianza del análisis: ${confidencePct}%. Descripción: "${excerpt}"`;
  }

  private async analyzeWithOpenAI(title: string, description: string): Promise<AnalysisResult> {
    // Placeholder for OpenAI integration
    // In production: call OpenAI chat completion with a structured prompt
    this.logger.warn('OpenAI integration placeholder — falling back to internal analysis');
    return this.analyzeInternal(title, description);
  }
}
