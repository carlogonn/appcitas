import { Router, Response, NextFunction } from 'express';
import prisma from '../../config/database';
import { authMiddleware, AuthRequest } from '../../middleware/auth.middleware';

const router = Router();

// Banned words list (in production, this would be in database)
const bannedWords = [
  // Spanish bad words
  'puta', 'puto', 'mierda', 'cabrón', 'cabrona', 'pendejo', 'pendeja',
  'estúpido', 'estúpida', 'idiota', 'imbecil', 'hp', 'hijo de puta',
  'malparido', 'malparida', 'gonorrea', 'gonorreona', 'marica', 'maricon',
  'maricona', 'coño', 'coñó', 'joder', 'jodido', 'maldito', 'maldita',
  'basura', 'asco', 'asqueroso', 'asquerosa', 'ascoso', 'ascosa',
  // English bad words
  'fuck', 'shit', 'bitch', 'ass', 'damn', 'asshole', 'bastard',
  'dick', 'pussy', 'cock', 'cunt', 'whore', 'slut', 'nigger', 'nigga',
  // Discriminatory words
  'negro', 'negra', 'chino', 'china', 'indio', 'india', 'maricon',
  'tortillero', 'sudaca', 'gringo', 'gringa',
];

// Severity levels
const severeWords = [
  'nigger', 'nigga', 'maricon', 'maricona', 'puto', 'puta',
  'hijo de puta', 'malparido', 'gonorrea',
];

// Check text for banned words
function checkText(text: string): { isClean: boolean; foundWords: string[]; severity: 'warning' | 'block' } {
  const normalizedText = text.toLowerCase()
    .normalize('NFD').replace(/[\u0300-\u036f]/g, '') // Remove accents
    .replace(/\s+/g, ' '); // Normalize spaces

  const foundWords: string[] = [];
  let maxSeverity: 'warning' | 'block' = 'warning';

  for (const word of bannedWords) {
    if (normalizedText.includes(word.toLowerCase())) {
      foundWords.push(word);
      if (severeWords.includes(word.toLowerCase())) {
        maxSeverity = 'block';
      }
    }
  }

  return {
    isClean: foundWords.length === 0,
    foundWords,
    severity: maxSeverity,
  };
}

// Check message before sending
router.post('/check', authMiddleware, async (req: AuthRequest, res: Response, next: NextFunction) => {
  try {
    const { text, context } = req.body; // context: 'chat' | 'community'

    if (!text || typeof text !== 'string') {
      res.status(400).json({ error: 'Text is required' });
      return;
    }

    const result = checkText(text);

    res.json({
      isClean: result.isClean,
      foundWords: result.foundWords,
      severity: result.severity,
      warning: result.isClean ? null : 'Tu mensaje contiene lenguaje inapropiado',
    });
  } catch (error) {
    next(error);
  }
});

// Get banned words (admin only)
router.get('/banned-words', authMiddleware, async (req: AuthRequest, res: Response, next: NextFunction) => {
  try {
    // TODO: Check if user is admin
    const words = await prisma.bannedWord.findMany({
      orderBy: { word: 'asc' },
    });

    res.json(words);
  } catch (error) {
    next(error);
  }
});

// Add banned word (admin only)
router.post('/banned-words', authMiddleware, async (req: AuthRequest, res: Response, next: NextFunction) => {
  try {
    // TODO: Check if user is admin
    const { word, severity = 'warning' } = req.body;

    if (!word || typeof word !== 'string') {
      res.status(400).json({ error: 'Word is required' });
      return;
    }

    const existingWord = await prisma.bannedWord.findUnique({
      where: { word: word.toLowerCase() },
    });

    if (existingWord) {
      res.status(400).json({ error: 'Word already exists' });
      return;
    }

    const newWord = await prisma.bannedWord.create({
      data: {
        word: word.toLowerCase(),
        severity,
      },
    });

    res.status(201).json(newWord);
  } catch (error) {
    next(error);
  }
});

// Delete banned word (admin only)
router.delete('/banned-words/:id', authMiddleware, async (req: AuthRequest, res: Response, next: NextFunction) => {
  try {
    // TODO: Check if user is admin
    const { id } = req.params;

    await prisma.bannedWord.delete({
      where: { id },
    });

    res.json({ message: 'Word deleted' });
  } catch (error) {
    next(error);
  }
});

export default router;
export { checkText };
