package org.example.gpt.service;

import lombok.RequiredArgsConstructor;
import org.example.challenge.entity.Challenge;
import org.example.challenge.entity.ChallengeTag;
import org.example.challenge.repository.ChallengeRepository;
import org.example.gpt.dto.ChallengeItem;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RecommendationService {

    private final ChallengeRepository challengeRepository;
    private final GptService gptService;

    /** 태그+프롬프트 기반 Top-3 추천: 엔티티로 반환 */
    public List<Challenge> recommendTop3ByTagAndPrompt(ChallengeTag tag, String userPrompt) {
        // 1) 태그로 후보 가져오기
        var list = challengeRepository.findByTagsContaining(tag);
        if (list.isEmpty()) return List.of();

        // 2) GPT에 넘길 최소 정보(id, title)만 구성
        var items = list.stream()
                .filter(c -> c.getChallengeName() != null && !c.getChallengeName().isBlank())
                .map(c -> new ChallengeItem(c.getChallengeId(), c.getChallengeName()))
                .toList();

        // 3) GPT 재랭킹으로 Top-3 id
        var topIds = gptService.pickTop3ByPrompt(userPrompt, items);
        if (topIds.isEmpty()) return List.of();

        // 4) id로 재조회 + GPT 순서대로 정렬
        var found = challengeRepository.findAllById(topIds);
        var order = new HashMap<Long, Integer>();
        for (int i = 0; i < topIds.size(); i++) order.put(topIds.get(i), i);

        return found.stream()
                .sorted(Comparator.comparingInt(c -> order.getOrDefault(c.getChallengeId(), Integer.MAX_VALUE)))
                .collect(Collectors.toList());
    }

    /** Top-3 id만 필요할 때 */
    public List<Long> recommendTop3Ids(ChallengeTag tag, String userPrompt) {
        var list = challengeRepository.findByTagsContaining(tag);
        if (list.isEmpty()) return List.of();

        var items = list.stream()
                .filter(c -> c.getChallengeName() != null && !c.getChallengeName().isBlank())
                .map(c -> new ChallengeItem(c.getChallengeId(), c.getChallengeName()))
                .toList();

        return gptService.pickTop3ByPrompt(userPrompt, items);
    }
}