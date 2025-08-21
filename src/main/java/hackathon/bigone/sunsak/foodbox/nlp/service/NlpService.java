package hackathon.bigone.sunsak.foodbox.nlp.service;

import hackathon.bigone.sunsak.foodbox.ocr.dto.OcrExtractedItem;
import jakarta.annotation.PostConstruct;
import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL;
import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.nlp.komoran.model.KomoranResult;
import kr.co.shineware.nlp.komoran.model.Token;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.Normalizer;
import java.util.*;

@Slf4j
@Service
public class NlpService {
    private Komoran komoran;

    // user_dict 단어 Set (메모리)
    private final Set<String> userDictWords = new HashSet<>();

    private static final int MIN_NOUN_LEN = 2; //2글자 이상

    private static boolean longEnough(String s) {
        return s != null && s.codePointCount(0, s.length()) >= MIN_NOUN_LEN;
    }

    private static String sanitize(String s) {
        if (s == null) return "";
        s = Normalizer.normalize(s, Normalizer.Form.NFKC);   // 폭/기호 정규화
        s = s.replaceAll("[^가-힣0-9a-zA-Z()\\s]", " "); //전처리
        s = s.replaceAll("\\s+", " ").trim(); //공백 압축
        return s;
    }

    @PostConstruct
    public void initKomoran() {
        try {
            komoran = new Komoran(DEFAULT_MODEL.FULL);

            // JAR 내부 파일을 임시 파일로 복사
            ClassPathResource resource = new ClassPathResource("data/user_dict.txt");
            Path tempFile = Files.createTempFile("user_dict", ".txt");

            try (InputStream in = resource.getInputStream()) {
                Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
            }

            // 메모리 Set에 단어 로드 ("단어\t태그" → 단어만 추출)
            try (BufferedReader br = Files.newBufferedReader(tempFile)) {
                String line;
                while ((line = br.readLine()) != null) {
                    String w = line.strip();
                    if (w.isEmpty()) continue;
                    int tab = w.indexOf('\t');
                    if (tab > 0) w = w.substring(0, tab).strip();
                    userDictWords.add(w);
                }
            }

            // Komoran에 사용자 사전 설정
            komoran.setUserDic(tempFile.toAbsolutePath().toString());
            System.out.println("KOMORAN 사용자 사전 로드 완료: " + tempFile);

            // 애플리케이션 종료 시 임시 파일 삭제
            tempFile.toFile().deleteOnExit();

        } catch (IOException e) {
            throw new RuntimeException("user_dict 로드 실패", e);
        }
    }

    /**
     * OCR 텍스트 목록을 받아서
     * - user_dict로 인식된 토큰 그룹
     * - user_dict에 없던 명사 그룹으로 분류하고, 수량 합산해서 반환
     */
    public ClassifiedTokens classifyByUserDict(List<OcrExtractedItem> rawItems) {
        Map<String, Integer> userDictGroup = new LinkedHashMap<>();
        Map<String, Integer> freeNounGroup = new LinkedHashMap<>();

        if (rawItems == null || rawItems.isEmpty()) {
            return new ClassifiedTokens(userDictGroup, freeNounGroup);
        }

        for (OcrExtractedItem item : rawItems) {
            if (item == null || item.getName() == null || item.getName().isBlank()) continue;

            String cleaned = sanitize(item.getName());
            KomoranResult res = komoran.analyze(cleaned);
            List<Token> tokens = res.getTokenList();
            int qty = Math.max(1, item.getQuantity());

            // user_dict 토큰 커버 범위 수집
            List<int[]> covered = new ArrayList<>();
            for (Token t : tokens) {
                String morph = t.getMorph();
                if (!longEnough(morph)) continue;
                if (morph == null || morph.isBlank()) continue;
                if (userDictWords.contains(morph)) {
                    userDictGroup.merge(morph, qty, Integer::sum);
                    covered.add(new int[]{t.getBeginIndex(), t.getEndIndex()}); // [begin, end)
                }
            }

            // 명사  중 user_dict 범위에 걸치지 않는 것만 자유명사로
            outer:
            for (Token t : tokens) {
                String pos = t.getPos();
                if (pos == null || !(pos.startsWith("NNG") || pos.startsWith("NNP"))) continue;

                String noun = t.getMorph();
                if (!longEnough(noun)) continue;
                if (noun == null || noun.isBlank()) continue;
                if (userDictWords.contains(noun)) continue; // user_dict와 일치하면 이미 처리됨

                int b = t.getBeginIndex(), e = t.getEndIndex();
                for (int[] c : covered) {
                    // 범위가 겹치면 skip (user_dict 토큰 내부 명사 제거)
                    if (!(e <= c[0] || b >= c[1])) continue outer;
                }
                freeNounGroup.merge(noun, qty, Integer::sum);
            }
        }
        if (rawItems != null && !rawItems.isEmpty()) {
            String joined = String.join(" | ",
                    rawItems.stream().map(OcrExtractedItem::getName).filter(Objects::nonNull).toList());
            log.debug("[NLP] input='{}'", joined);
        }
        log.debug("[NLP] userDictKeys={}, freeNounKeys={}",
                userDictGroup.keySet(), freeNounGroup.keySet());

        return new ClassifiedTokens(userDictGroup, freeNounGroup);
    }

    /**
     * (옵션) 명사만 뽑아 수량과 함께 반환 — 디버깅/로그 확인용
     */
    public List<OcrExtractedItem> extractNouns(List<OcrExtractedItem> rawItems) {
        List<OcrExtractedItem> result = new ArrayList<>();
        if (rawItems == null || rawItems.isEmpty()) return result;

        for (OcrExtractedItem item : rawItems) {
            if (item == null || item.getName() == null || item.getName().isBlank()) continue;

            String cleaned = sanitize(item.getName());
            KomoranResult res = komoran.analyze(cleaned);
            List<String> nouns = res.getNouns();
            int qty = Math.max(1, item.getQuantity());

            for (String noun : nouns) {
                if (!longEnough(noun)) continue;
                if (noun == null || noun.isBlank()) continue;
                result.add(new OcrExtractedItem(noun, qty));
            }
        }
        return result;
    }

    @Getter
    public static class ClassifiedTokens {
        private final Map<String, Integer> userDict; // user_dict 단어 그룹
        private final Map<String, Integer> freeNouns; // 일반 명사 그룹

        public ClassifiedTokens(Map<String, Integer> userDict, Map<String, Integer> freeNouns) {
            this.userDict = userDict;
            this.freeNouns = freeNouns;
        }
    }
}
