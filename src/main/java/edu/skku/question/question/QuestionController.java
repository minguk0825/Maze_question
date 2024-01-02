package edu.skku.question.question;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.skku.question.question.dto.CreateQuestionDto;
import edu.skku.question.question.dto.QuestionAnswerDto;
import edu.skku.question.question.dto.QuestionResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class QuestionController {
    private final QuestionService questionService;
    private final OpenAiService openAiService;
    private final ObjectMapper objectMapper;

    // 요청된 타입의 문제 하나 랜덤으로 가져가기
    @GetMapping("random")
    public QuestionResponseDto getRandomQuestion(
            @RequestParam("type") QuestionType questionType,
            @RequestParam("level") QuestionLevel questionLevel) {
        Question question = this.questionService.findRandom(questionType, questionLevel);
        return new QuestionResponseDto(question.getId(), question.getText(), question.getChoices(), question.getType(), question.getAnswer(), question.getLevel());
    }

    // 문제 생성해서 DB에 저장하기
    @PostMapping("question")
    public void createQuestion(@RequestBody CreateQuestionDto dto) {
        this.questionService.createQuestion(dto.getText(), dto.getType(), dto.getChoices(), dto.getAnswer(), dto.getLevel());
    }

    // 요청된 정답이 맞는지 틀렸는지 보내주기
    @PostMapping("answer")
    public Boolean isAnswer(@RequestBody QuestionAnswerDto dto) {
        System.out.println("dto.getQuestionId() = " + dto.getQuestionId());
        Question question = questionService.findOne(dto.getQuestionId());
        return question.getAnswer().equals(dto.getAnswer());
    }

    // 서버에서 특정 문제 삭제 api
    @DeleteMapping("question/{id}")
    public void deleteQuestion(@PathVariable Long id) {
        this.questionService.deleteQuestion(id);
    }

    // 여러 문제 한번에 생성 api
    @PostMapping("questions")
    public void createQuestions(@RequestBody List<CreateQuestionDto> questionDtos) {
        this.questionService.createQuestions(questionDtos);
    }

    // 모든 문제 삭제 api
    @DeleteMapping("reset")
    public void deleteAllQuestions() {
        this.questionService.deleteAllQuestions();
    }

    // 모든 문제 출력 api


    // openai 로 문제 생성
    @SneakyThrows
    @PostMapping("generate-questions-math")
    public void generateAndSaveQuestionsMath() {
        // 질문 생성 및 저장 로직
        String prompt = "Generate an Math multiple-choice question suitable for elementary to middle school students level ,문제 레벨별 생성 비율을 TWO,THREE,FOUR 같은 비중으로 생성해줘  ONE은 0% 비중으로 문제를 생성해줘 text의 내용이 레벨 조건에 맞도록 생성하고 조건에 맞는 level을 넣어 구성해줘  레벨 FOUR 생성해줘  \n" +
                "\n" +
                "1. Level ONE: Simple single-digit addition or subtraction.\n" +
                "2. Level TWO: Addition or subtraction with two-digit numbers, or single-digit multiplication or division.\n" +
                "3. Level THREE: Addition or subtraction between two-digit numbers, or multiplication or division involving two-digit numbers.\n" +
                "4. Level FOUR: Addition or subtraction with three-digit numbers, or multiplication or division between two-digit numbers.\n" +
                "\n" +
                "Provide the problem in JSON format with four answer choices, indicating the correct answer.\n" +
                "\n" +
                "Example:\n" +
                "{\n" +
                "    \"text\": \"34 + 21 = ?\",\n" +
                "    \"choices\": [\n" +
                "        {\"number\": 1, \"text\": \"54\", \"isAnswer\": false},\n" +
                "        {\"number\": 2, \"text\": \"55\", \"isAnswer\": true},\n" +
                "        {\"number\": 3, \"text\": \"56\", \"isAnswer\": false},\n" +
                "        {\"number\": 4, \"text\": \"57\", \"isAnswer\": false}\n" +
                "    ],\n" +
                "    \"type\": \"MATH\",\n" +
                "    \"answer\": 2,\n" +
                "    \"level\": \"Select FOUR\"\n" +
                "이 요청에 맞는 문제를 생성해줘 그리고 생성된 문제가 답이 확실한지 choice의 text내용에 중복이 없게 문제를 생성해줘}\n"; // 질문 생성 prompt
        String generatedQuestion = openAiService.generateQuestion(prompt);
        //Open Ai 응답에서 질문 데이터 파싱 및 변환
        // JSON 형식의 응답을 CreateQuestionDto 객체로 변환
        try {
            JsonNode responseJson = objectMapper.readTree(generatedQuestion);
            String questionContent = responseJson.path("choices").get(0).path("message").path("content").asText();
            CreateQuestionDto questionDto = objectMapper.readValue(questionContent, CreateQuestionDto.class);
            System.out.println(questionDto.getText());
            questionService.createQuestion(questionDto);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse OpenAI response", e);
        }
    }

    @GetMapping("random-questions")
    public List<QuestionResponseDto> getRandomQuestions(
            @RequestParam("type") QuestionType questionType,
            @RequestParam("level") QuestionLevel questionLevel,
            @RequestParam("count") int count
    ) {
        List<Question> questions = this.questionService.findRandomQuestions(questionType, questionLevel, count);
        return questions.stream()
                .map(question -> new QuestionResponseDto(question.getId(), question.getText(), question.getChoices(), question.getType(), question.getAnswer(), question.getLevel()))
                .collect(Collectors.toList());
    }

    @SneakyThrows
    @PostMapping("generate-questions-english")
    public void generateAndSaveQuestionsEnglish() {
        // 질문 생성 및 저장 로직
        String prompt = "Generate an English vocabulary multiple-choice question suitable for elementary to middle school students, randomly chosen from one of four difficulty levels:\\n\\n1. Level ONE: Basic words commonly used in elementary school.\\n2. Level TWO: Words and phrases used in upper elementary or early middle school.\\n3. Level THREE: Intermediate words typically encountered in middle school.\\n4. Level FOUR: More challenging vocabulary that middle school students are likely to encounter.\\n\\nFormat the question with a word and four choices, indicating the correct answer. The choices and text should be written in Korean.레벨 FOUR 생성해줘" +
                "\n" +
                "1. Level ONE: Basic vocabulary for elementary students.\n" +
                "2. Level TWO: Moderately challenging vocabulary for upper elementary or early middle school.\n" +
                "3. Level THREE: Intermediate vocabulary for middle school.\n" +
                "4. Level FOUR: Challenging vocabulary for middle school.\n" +
                "\n" +
                "Provide the problem in JSON format with four answer choices, indicating the correct answer.\n" +
                "\n" +
                "Example:\n" +
                "{\n" +
                "    \"text\": \"Select the correct meaning of the word: 'cat'\",\n" +
                "    \"choices\": [\n" +
                "        {\"number\": 1, \"text\": \"고양이\", \"isAnswer\": true},\n" +
                "        {\"number\": 2, \"text\": \"자동차\", \"isAnswer\": false},\n" +
                "        {\"number\": 3, \"text\": \"배\", \"isAnswer\": false},\n" +
                "        {\"number\": 4, \"text\": \"모자\", \"isAnswer\": false}\n" +
                "    ],\n" +
                "    \"type\": \"ENGLISH\",\n" +
                "    \"answer\": 1,\n" +
                "    \"level\": \"Select FOUR\"\n" +
                "}\n"; // 질문 생성 prompt
        String generatedQuestion = openAiService.generateQuestion(prompt);

        //Open Ai 응답에서 질문 데이터 파싱 및 변환
        // JSON 형식의 응답을 CreateQuestionDto 객체로 변환
        try {
            JsonNode responseJson = objectMapper.readTree(generatedQuestion);
            String questionContent = responseJson.path("choices").get(0).path("message").path("content").asText();
            CreateQuestionDto questionDto = objectMapper.readValue(questionContent, CreateQuestionDto.class);
            System.out.println(questionDto.getText());
            questionService.createQuestion(questionDto);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse OpenAI response", e);
        }


    }

    @SneakyThrows
    @PostMapping("generate-questions-idiom")
    public void generateAndSaveQuestionsIdiom() {
        // 질문 생성 및 저장 로직
        String prompt = "초등학생부터 중학생 수준에 적합한 사자성어 문제를 네 가지 난이도로 생성하십시오.\\n\\n1. Level ONE: 초등학생을 위한 기본적인 사자성어 문제.\\n2. Level TWO: 초등학생 고학년부터 중학생 초반을 위한 약간 어려운 사자성어 문제.\\n3. Level THREE: 중학생을 위한 중간 난이도의 사자성어 문제.\\n4. Level FOUR: 중학생에게 도전적인 사자성어 문제.\\n\\n문제를 네 개의 선택지와 함께 제공하고, 올바른 답을 지정하세요. 문제와 선택지는 교육적이고 정보적인 내용을 담아야 합니다.질문과 선택지는 한국어로 작성되어야 합니다. level 에서 ONE TWO THREE 은 제외하고 FOUR 레벨만 생성해줘" +
                "\n" +
                "1. Level ONE: 초등학생을 위한 기본적인 사자성어.\n" +
                "2. Level TWO: 초등학생 고학년부터 중학생 초반을 위한 어려운 사자성어.\n" +
                "3. Level THREE: 중학생을 위한 중간 난이도의 사자성어.\n" +
                "4. Level FOUR: 중학생에게 도전적인 사자성어.\n" +
                "\n" +
                "Provide the problem in JSON format with four answer choices, indicating the correct answer.모든 질문의 type은 'IDIOM'로 지정해주세요\n" +
                "\n" +
                "Example:\n" +
                "{\n" +
                "    \"text\": \"다음 사자성어의 뜻은 무엇입니까: '일석이조'\",\n" +
                "    \"choices\": [\n" +
                "        {\"number\": 1, \"text\": \"한 가지 일을 하면서 두 가지 이득을 얻는다\", \"isAnswer\": true},\n" +
                "        {\"number\": 2, \"text\": \"매사에 조심해야 한다\", \"isAnswer\": false},\n" +
                "        {\"number\": 3, \"text\": \"한 가지 일에 모든 것을 건다\", \"isAnswer\": false},\n" +
                "        {\"number\": 4, \"text\": \"결과가 좋지 않을 것이다\", \"isAnswer\": false}\n" +
                "    ],\n" +
                "    \"type\": \"IDIOM\",\n" +
                "    \"answer\": 1,\n" +
                "    \"level\": \"Select only FOUR \"\n" +
                "}\n"; // 질문 생성 prompt
        String generatedQuestion = openAiService.generateQuestion(prompt);

        //Open Ai 응답에서 질문 데이터 파싱 및 변환
        // JSON 형식의 응답을 CreateQuestionDto 객체로 변환
        try {
            JsonNode responseJson = objectMapper.readTree(generatedQuestion);
            String questionContent = responseJson.path("choices").get(0).path("message").path("content").asText();
            CreateQuestionDto questionDto = objectMapper.readValue(questionContent, CreateQuestionDto.class);
            System.out.println(questionDto.getText());
            questionService.createQuestion(questionDto);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse OpenAI response", e);
        }
    }

    @SneakyThrows
    @PostMapping("generate-questions-sense")
    public void generateAndSaveQuestionsSense() {
        // 질문 생성 및 저장 로직
        String prompt = "한 개의 일반 지식 질문을 생성해주세요. 이 질문은 초등학생에서 중학생 초반 학년을 대상으로 하는 문제를 생성해줘 ONE 레벨은 제외하고 생성해줘:레벨 FOUR 생성해줘\n" +
                "\n" +
                "레벨 ONE: 초등학생을 위한 기본 일반 지식 질문.\n" +
                "레벨 TWO: 초등학교 고학년을 위한 약간 더 도전적인 질문.\n" +
                "레벨 THREE: 중학교로 전환하는 학생들을 위한 중급 질문.\n" +
                "레벨 FOUR: 중학교 초반 학생들을 위한 보다 도전적인 질문.\n" +
                "질문은 네 개의 선택지와 함께 제공되어야 하며, 정답을 나타내야 합니다. 질문과 선택지는 한국어로 작성되어야 합니다. 모든 질문의 type은 'SENSE'로 지정해주세요." +
                "Provide the problem in JSON format with four answer choices, indicating the correct answer.\n" +
                "\n" +
                "Example:\n" +
                "{\n" +
                "    \"text\": \"태양계에서 가장 큰 행성은 무엇입니까?\",\n" +
                "    \"choices\": [\n" +
                "        {\"number\": 1, \"text\": \"지구\", \"isAnswer\": false},\n" +
                "        {\"number\": 2, \"text\": \"목성\", \"isAnswer\": true},\n" +
                "        {\"number\": 3, \"text\": \"화성\", \"isAnswer\": false},\n" +
                "        {\"number\": 4, \"text\": \"토성\", \"isAnswer\": false}\n" +
                "    ],\n" +
                "    \"type\": \"SENSE\",\n" +
                "    \"answer\": 2,\n" +
                "    \"level\": \"Select FOUR\"\n" +
                "}\n"; // 질문 생성 prompt
        String generatedQuestion = openAiService.generateQuestion(prompt);

        //Open Ai 응답에서 질문 데이터 파싱 및 변환
        // JSON 형식의 응답을 CreateQuestionDto 객체로 변환
        try {
            JsonNode responseJson = objectMapper.readTree(generatedQuestion);
            String questionContent = responseJson.path("choices").get(0).path("message").path("content").asText();
            CreateQuestionDto questionDto = objectMapper.readValue(questionContent, CreateQuestionDto.class);
            System.out.println(questionDto.getText());
            questionService.createQuestion(questionDto);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse OpenAI response", e);
        }
    }

    @SneakyThrows
    @PostMapping("generate-questions-proberb")
    public void generateAndSaveQuestionsProberb() {
        // 질문 생성 및 저장 로직
        String prompt = "초등학생부터 중학생 수준에 적합한 속담 문제를 네 가지 난이도로 생성하십시오.\\n\\n1. Level ONE: 초등학생을 위한 간단한 속담 문제.\\n2. Level TWO: 초등학생 고학년부터 중학생 초반을 위한 약간 어려운 속담 문제.\\n3. Level THREE: 중학생을 위한 중간 난이도의 속담 문제.\\n4. Level FOUR: 중학생에게 도전적인 속담 문제.\\n\\n문제를 네 개의 선택지와 함께 제공하고, 올바른 답을 지정하세요. 문제와 선택지는 교육적이고 정보적인 내용을 담아야 합니다.질문과 선택지는 한국어로 작성되어야 합니다.\n" +
                "\n" +
                "1. Level ONE: Simple single-digit addition or subtraction.\n" +
                "2. Level TWO: Addition or subtraction with two-digit numbers, or single-digit multiplication or division.\n" +
                "3. Level THREE: Addition or subtraction between two-digit numbers, or multiplication or division involving two-digit numbers.\n" +
                "4. Level FOUR: Addition or subtraction with three-digit numbers, or multiplication or division between two-digit numbers.\n" +
                "\n" +
                "Provide the problem in JSON format with four answer choices, indicating the correct answer.\n" +
                "\n" +
                "Example:\n" +
                "{\n" +
                "    \"text\": \"다음 속담의 뜻은 무엇입니까: '가는 날이 장날'\",\n" +
                "    \"choices\": [\n" +
                "        {\"number\": 1, \"text\": \"\"장날에는 항상 비가 온다\"\", \"isAnswer\": false},\n" +
                "        {\"number\": 2, \"text\": \"중요한 날에는 언제나 문제가 생긴다\", \"isAnswer\": true},\n" +
                "        {\"number\": 3, \"text\": \"날씨가 좋은 날은 항상 바쁘다\", \"isAnswer\": false},\n" +
                "        {\"number\": 4, \"text\": \"모든 일이 계획대로 진행된다\", \"isAnswer\": false}\n" +
                "    ],\n" +
                "    \"type\": \"PROBERB\",\n" +
                "    \"answer\": 2,\n" +
                "    \"level\": \"Select randomly from ONE, TWO, THREE, FOUR\"\n" +
                "}\n"; // 질문 생성 prompt
        String generatedQuestion = openAiService.generateQuestion(prompt);

        //Open Ai 응답에서 질문 데이터 파싱 및 변환
        // JSON 형식의 응답을 CreateQuestionDto 객체로 변환
        try {
            JsonNode responseJson = objectMapper.readTree(generatedQuestion);
            String questionContent = responseJson.path("choices").get(0).path("message").path("content").asText();
            CreateQuestionDto questionDto = objectMapper.readValue(questionContent, CreateQuestionDto.class);
            System.out.println(questionDto.getText());
            questionService.createQuestion(questionDto);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse OpenAI response", e);
        }


    }
}
