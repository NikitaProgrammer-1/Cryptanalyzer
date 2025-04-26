import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class CaesarCipher {
    private static final char[] ALPHABET = {
            'а', 'б', 'в', 'г', 'д', 'е', 'ж', 'з', 'и', 'й', 'к', 'л', 'м', 'н', 'о',
            'п', 'р', 'с', 'т', 'у', 'ф', 'х', 'ц', 'ч', 'ш', 'щ', 'ъ', 'ы', 'ь', 'э', 'ю', 'я',
            '.', ',', '«', '»', '"', '\'', ':', '-', '!', '?', ' '
    };

    private static final int ALPHABET_SIZE = ALPHABET.length;

    public static String encrypt(String text, int key) {
        key = key % ALPHABET_SIZE;
        if (key < 0) {
            key += ALPHABET_SIZE;
        }

        StringBuilder encryptedText = new StringBuilder();

        for (char c : text.toLowerCase().toCharArray()) {
            int index = findCharIndex(c);
            if (index != -1) {
                int newIndex = (index + key) % ALPHABET_SIZE;
                encryptedText.append(ALPHABET[newIndex]);
            } else {
                encryptedText.append(c);
            }
        }

        return encryptedText.toString();
    }

    public static String decrypt(String text, int key) {
        key = key % ALPHABET_SIZE;
        if (key < 0) {
            key += ALPHABET_SIZE;
        }

        return encrypt(text, ALPHABET_SIZE - key);
    }

    public static String bruteForce(String encryptedText) {
        String bestDecryption = "";
        int bestKey = 0;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (int key = 0; key < ALPHABET_SIZE; key++) {
            String decrypted = decrypt(encryptedText, key);
            double score = evaluateDecryption(decrypted);

            if (score > bestScore) {
                bestScore = score;
                bestDecryption = decrypted;
                bestKey = key;
            }
        }

        System.out.println("Найденный ключ: " + bestKey);
        return bestDecryption;
    }

    public static String statisticalDecrypt(String encryptedText, String representativeText) {
        Map<Character, Double> encryptedFreq = calculateFrequency(encryptedText);
        Map<Character, Double> representativeFreq = calculateFrequency(representativeText);

        int bestShift = 0;
        double bestCorrelation = Double.NEGATIVE_INFINITY;

        for (int shift = 0; shift < ALPHABET_SIZE; shift++) {
            double correlation = 0;

            for (Map.Entry<Character, Double> entry : representativeFreq.entrySet()) {
                char c = entry.getKey();
                int index = findCharIndex(c);
                if (index != -1) {
                    int shiftedIndex = (index + shift) % ALPHABET_SIZE;
                    char shiftedChar = ALPHABET[shiftedIndex];
                    correlation += entry.getValue() * encryptedFreq.getOrDefault(shiftedChar, 0.0);
                }
            }

            if (correlation > bestCorrelation) {
                bestCorrelation = correlation;
                bestShift = shift;
            }
        }

        System.out.println("Определенный сдвиг: " + bestShift);
        return decrypt(encryptedText, bestShift);
    }

    private static Map<Character, Double> calculateFrequency(String text) {
        Map<Character, Integer> counts = new HashMap<>();
        int total = 0;

        for (char c : text.toLowerCase().toCharArray()) {
            if (findCharIndex(c) != -1) {
                counts.put(c, counts.getOrDefault(c, 0) + 1);
                total++;
            }
        }

        Map<Character, Double> frequencies = new HashMap<>();
        for (Map.Entry<Character, Integer> entry : counts.entrySet()) {
            frequencies.put(entry.getKey(), (double) entry.getValue() / total);
        }

        return frequencies;
    }

    private static double evaluateDecryption(String text) {
        int spaceCount = 0;
        int punctuationCount = 0;
        int wordLength = 0;
        int wordCount = 0;

        for (char c : text.toCharArray()) {
            if (c == ' ') {
                spaceCount++;
                if (wordLength > 0) {
                    wordCount++;
                    wordLength = 0;
                }
            } else if (c == '.' || c == '!' || c == '?') {
                punctuationCount++;
            } else if (findCharIndex(c) != -1) {
                wordLength++;
            }
        }

        return spaceCount * 0.5 + punctuationCount * 0.3 + (wordCount > 0 ? 10.0 / (1 + Math.abs(5 - (text.length() - spaceCount) / (double) wordCount)) : 0);
    }

    private static int findCharIndex(char c) {
        for (int i = 0; i < ALPHABET_SIZE; i++) {
            if (ALPHABET[i] == c) {
                return i;
            }
        }
        return -1;
    }

    public static String readFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        byte[] bytes = Files.readAllBytes(path);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static void writeFile(String content, String filePath) throws IOException {
        Path path = Paths.get(filePath);
        Files.write(path, content.getBytes(StandardCharsets.UTF_8));
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n=== Шифр Цезаря ===");
            System.out.println("1. Зашифровать текст");
            System.out.println("2. Расшифровать текст с известным ключом");
            System.out.println("3. Взломать шифр перебором (Brute Force)");
            System.out.println("4. Взломать шифр статистическим анализом");
            System.out.println("0. Выход");
            System.out.print("Выберите действие: ");

            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите число от 0 до 4");
                continue;
            }

            try {
                switch (choice) {
                    case 1:
                        System.out.print("Введите путь к исходному файлу: ");
                        String inputFile = scanner.nextLine();
                        System.out.print("Введите путь для зашифрованного файла: ");
                        String outputFile = scanner.nextLine();
                        System.out.print("Введите ключ шифрования (число): ");
                        int encryptKey = Integer.parseInt(scanner.nextLine());

                        String textToEncrypt = readFile(inputFile);
                        String encryptedText = encrypt(textToEncrypt, encryptKey);
                        writeFile(encryptedText, outputFile);
                        System.out.println("Текст успешно зашифрован и сохранен в " + outputFile);
                        break;

                    case 2:
                        System.out.print("Введите путь к зашифрованному файлу: ");
                        String encryptedFile = scanner.nextLine();
                        System.out.print("Введите путь для расшифрованного файла: ");
                        String decryptedFile = scanner.nextLine();
                        System.out.print("Введите ключ шифрования (число): ");
                        int decryptKey = Integer.parseInt(scanner.nextLine());

                        String textToDecrypt = readFile(encryptedFile);
                        String decryptedText = decrypt(textToDecrypt, decryptKey);
                        writeFile(decryptedText, decryptedFile);
                        System.out.println("Текст успешно расшифрован и сохранен в " + decryptedFile);
                        break;

                    case 3:
                        System.out.print("Введите путь к зашифрованному файлу: ");
                        String bruteForceFile = scanner.nextLine();
                        System.out.print("Введите путь для расшифрованного файла: ");
                        String bruteForceOutput = scanner.nextLine();

                        String bruteForceText = readFile(bruteForceFile);
                        String bruteForceResult = bruteForce(bruteForceText);
                        writeFile(bruteForceResult, bruteForceOutput);
                        System.out.println("Результат brute force сохранен в " + bruteForceOutput);
                        break;

                    case 4:
                        System.out.print("Введите путь к зашифрованному файлу: ");
                        String statFile = scanner.nextLine();
                        System.out.print("Введите путь к файлу с примером текста (для анализа): ");
                        String sampleFile = scanner.nextLine();
                        System.out.print("Введите путь для расшифрованного файла: ");
                        String statOutput = scanner.nextLine();

                        String statText = readFile(statFile);
                        String sampleText = readFile(sampleFile);
                        String statResult = statisticalDecrypt(statText, sampleText);
                        writeFile(statResult, statOutput);
                        System.out.println("Результат статистического анализа сохранен в " + statOutput);
                        break;

                    case 0:
                        System.out.println("Выход из программы...");
                        scanner.close();
                        return;

                    default:
                        System.out.println("Ошибка: неверный выбор. Попробуйте снова.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: ключ должен быть целым числом");
            } catch (IOException e) {
                System.out.println("Ошибка при работе с файлом: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Произошла ошибка: " + e.getMessage());
            }
        }
    }
}