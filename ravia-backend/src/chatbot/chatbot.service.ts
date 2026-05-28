import { Injectable, Logger } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';

export interface ChatRequest {
  message: string;
  sessionId?: string;
}

export interface ChatResponse {
  reply: string;
  suggestions?: string[];
}

interface TopicRule {
  keywords: string[];
  reply: string;
  suggestions?: string[];
}

const TOPIC_RULES: TopicRule[] = [
  {
    keywords: ['incendio', 'fuego', 'quemando', 'llamas', 'humo'],
    reply:
      '**En caso de incendio:**\n\n1. Llama inmediatamente al 911.\n2. Activa la alarma de incendio si hay una.\n3. Evacua el edificio sin usar elevador.\n4. Cierra puertas al salir para retrasar el avance del fuego.\n5. Si hay humo, desplazate agachado.\n6. No regreses por pertenencias.\n7. Reunete en el punto de encuentro designado.\n\nReporta el incendio en RAVIA para alertar a vecinos. Este chat no sustituye al 911.',
    suggestions: ['Como reportar en RAVIA', 'Como evacuar', 'Numeros de emergencia'],
  },
  {
    keywords: ['accidente', 'choque', 'colision', 'atropellado'],
    reply:
      '**En caso de accidente vial:**\n\n1. Llama al 911 de inmediato.\n2. No muevas a los heridos salvo peligro inminente.\n3. Enciende luces de emergencia y coloca senalamientos si es seguro.\n4. Presta primeros auxilios solo si estas capacitado.\n5. Documenta con fotos cuando no te ponga en riesgo.\n6. No obstruyas los carriles libres.\n\nReporta el accidente en RAVIA para advertir a la comunidad.',
    suggestions: ['Primeros auxilios', 'Datos para emergencias', 'Como reportar en RAVIA'],
  },
  {
    keywords: ['herido', 'inconsciente', 'desmayado', 'sangre', 'medico', 'emergencia medica'],
    reply:
      '**Emergencia medica:**\n\n1. Llama al 911 o pide una ambulancia.\n2. Manten a la persona acompanada y hablale con calma.\n3. No le des comida ni bebida.\n4. Si no respira y sabes hacerlo, inicia RCP.\n5. Controla hemorragias con presion directa.\n6. Manten abrigada a la persona.\n7. No retires objetos incrustados.\n\nSi hay riesgo inmediato, prioriza llamar al 911.',
    suggestions: ['Como hacer RCP', 'Controlar hemorragia', 'Que datos dar al 911'],
  },
  {
    keywords: ['inundacion', 'inundado', 'agua', 'desbordamiento', 'lluvia intensa'],
    reply:
      '**En caso de inundacion:**\n\n1. Evacua si las autoridades lo indican.\n2. Dirigete a zonas altas.\n3. Desconecta electricidad solo si puedes hacerlo sin riesgo.\n4. No cruces corrientes de agua a pie ni en auto.\n5. Evita contacto con agua contaminada.\n6. Lleva documentos y medicamentos importantes.\n\nSigue las alertas oficiales y usa RAVIA para avisar a vecinos.',
    suggestions: ['Kit de emergencia', 'Que hacer si quedo atrapado', 'Reportar inundacion'],
  },
  {
    keywords: ['gas', 'fuga de gas', 'olor a gas'],
    reply:
      '**En caso de fuga de gas:**\n\n1. No enciendas aparatos electricos ni cerillos.\n2. Abre puertas y ventanas si puedes hacerlo con seguridad.\n3. Cierra la llave principal del gas.\n4. Evacua el inmueble.\n5. Llama a tu compania de gas o al 911.\n6. No regreses hasta que lo autoricen.\n7. Avisa a vecinos cercanos.\n\nPara emergencias reales, llama al 911.',
    suggestions: ['Ubicar llave de gas', 'Numeros de emergencia', 'Reportar fuga'],
  },
  {
    keywords: ['sismo', 'terremoto', 'temblor'],
    reply:
      '**En caso de sismo:**\n\nDurante el movimiento, alejate de ventanas y objetos que puedan caer, protegete bajo una mesa solida o junto a una zona estructural segura, y evita correr.\n\nDespues, revisa heridos, corta gas si hueles fuga, sal por escaleras, alejate de estructuras danadas y reporta riesgos en RAVIA.',
    suggestions: ['Mochila de emergencia', 'Que hacer ante derrumbe', 'Reportar danos'],
  },
  {
    keywords: ['robo', 'asalto', 'ladron', 'delincuente', 'amenaza'],
    reply:
      '**En caso de robo o asalto:**\n\n1. No te resistas si hay armas o amenaza directa.\n2. Mantente lo mas calmado posible.\n3. Observa rasgos utiles solo si no te pone en peligro.\n4. Cuando sea seguro, llama al 911.\n5. No contamines la escena.\n6. Haz una denuncia ante la autoridad correspondiente.\n7. Reporta en RAVIA para alertar a vecinos.\n\nEste chat no sustituye a servicios de emergencia.',
    suggestions: ['Como denunciar', 'Como reportar en RAVIA', 'Datos utiles del incidente'],
  },
  {
    keywords: ['reportar', 'crear reporte', 'como reporto', 'como se reporta'],
    reply:
      '**Como crear un reporte en RAVIA:**\n\n1. Toca el boton de reportar.\n2. Selecciona la categoria del incidente.\n3. Escribe titulo y descripcion claros.\n4. Confirma la ubicacion.\n5. Agrega foto o evidencia si es seguro.\n6. Revisa el analisis de IA.\n7. Confirma y publica.\n\nOtros vecinos podran confirmar, desmentir o aportar informacion.',
    suggestions: ['Que pasa con mi reporte', 'Como confirmar reportes', 'Reportar de forma anonima'],
  },
  {
    keywords: ['numero', 'telefono', 'emergencia', 'llamar', 'contacto'],
    reply:
      '**Numeros de emergencia en Mexico:**\n\n911: emergencias generales.\n089: denuncia anonima.\n071: CFE.\n\nTambien contacta servicios locales de bomberos, proteccion civil o cruz roja segun tu municipio. Para emergencias reales, llama al 911.',
    suggestions: ['Que datos dar al 911', 'Como reportar en RAVIA', 'Emergencia medica'],
  },
];

const DEFAULT_REPLY =
  'Hola, soy el asistente de RAVIA.\n\nPuedo orientarte sobre incendios, accidentes viales, emergencias medicas, inundaciones, fugas de gas, sismos, robos, reportes y numeros de emergencia.\n\nPara emergencias reales, llama al 911. Este chat solo brinda orientacion basica.';

@Injectable()
export class ChatbotService {
  private readonly logger = new Logger(ChatbotService.name);

  constructor(private readonly config: ConfigService) {}

  async chat(dto: ChatRequest): Promise<ChatResponse> {
    const groqReply = await this.tryGroq(dto);
    if (groqReply) {
      return {
        reply: groqReply,
        suggestions: this.getContextualSuggestions(dto.message),
      };
    }

    return this.localChat(dto);
  }

  private async tryGroq(dto: ChatRequest): Promise<string | null> {
    const apiKey = this.config.get<string>('GROQ_API_KEY');
    if (!apiKey) return null;

    const model = this.config.get<string>('GROQ_MODEL') ?? 'llama-3.3-70b-versatile';
    const controller = new AbortController();
    const timeout = setTimeout(() => controller.abort(), 15000);

    try {
      const response = await fetch('https://api.groq.com/openai/v1/chat/completions', {
        method: 'POST',
        signal: controller.signal,
        headers: {
          Authorization: `Bearer ${apiKey}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          model,
          temperature: 0.25,
          max_tokens: 750,
          messages: [
            {
              role: 'system',
              content:
                'Eres el asistente de seguridad vecinal de RAVIA. Responde en espanol claro, breve y accionable. ' +
                'Prioriza seguridad fisica, recomienda llamar al 911 ante emergencias reales y no inventes informacion oficial. ' +
                'Puedes explicar como usar la app: crear reportes, confirmar reportes, revisar alertas, zonas de riesgo y fichas de busqueda. ' +
                'No pidas datos sensibles innecesarios. Si el usuario describe riesgo inmediato, indica pasos concretos y seguros.',
            },
            {
              role: 'user',
              content: dto.message,
            },
          ],
        }),
      });

      if (!response.ok) {
        this.logger.warn(`Groq request failed with status ${response.status}`);
        return null;
      }

      const data = await response.json() as {
        choices?: Array<{ message?: { content?: string } }>;
      };

      return data.choices?.[0]?.message?.content?.trim() || null;
    } catch (error) {
      this.logger.warn(`Groq request failed: ${(error as Error).message}`);
      return null;
    } finally {
      clearTimeout(timeout);
    }
  }

  private localChat(dto: ChatRequest): ChatResponse {
    const text = dto.message.toLowerCase().trim();

    for (const rule of TOPIC_RULES) {
      if (rule.keywords.some((kw) => text.includes(kw))) {
        return { reply: rule.reply, suggestions: rule.suggestions };
      }
    }

    if (text.match(/^(hola|hi|hello|buenos|buenas|hey)/)) {
      return {
        reply: DEFAULT_REPLY,
        suggestions: ['Que hacer en un incendio', 'Numeros de emergencia', 'Como reportar en RAVIA'],
      };
    }

    return {
      reply: `No encontre informacion especifica sobre "${dto.message}". Para emergencias reales, llama al 911.\n\nPuedo ayudarte con incendios, accidentes, emergencias medicas, inundaciones, fugas de gas, sismos, robos o uso de la app.`,
      suggestions: ['Numeros de emergencia', 'Que hacer en un incendio', 'Como usar RAVIA'],
    };
  }

  private getContextualSuggestions(message: string): string[] {
    const text = message.toLowerCase().trim();
    const rule = TOPIC_RULES.find((topic) => topic.keywords.some((kw) => text.includes(kw)));
    return rule?.suggestions ?? ['Numeros de emergencia', 'Como reportar en RAVIA', 'Ver alertas cercanas'];
  }

  getSuggestions() {
    return [
      { id: '1', text: 'Que hacer en un incendio', query: 'incendio' },
      { id: '2', text: 'Emergencias medicas', query: 'emergencia medica' },
      { id: '3', text: 'Fuga de gas', query: 'fuga de gas' },
      { id: '4', text: 'Inundaciones', query: 'inundacion' },
      { id: '5', text: 'Numeros de emergencia', query: 'numeros de emergencia' },
      { id: '6', text: 'Como reportar en RAVIA', query: 'como reportar' },
    ];
  }
}
