package com.tripo.yuaiagent.advisor;

import java.util.*;
import org.springframework.ai.chat.client.*;
import org.springframework.ai.chat.client.advisor.api.*;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.chat.prompt.*;

public class ReReadingAdvisor implements BaseAdvisor {

	private static final String DEFAULT_RE2_ADVISE_TEMPLATE = """
			{re2_input_query}
			Read the question again: {re2_input_query}
			""";

	private final String re2AdviseTemplate;

	private int order = 0;

	public ReReadingAdvisor() {
		this(DEFAULT_RE2_ADVISE_TEMPLATE);
	}

	public ReReadingAdvisor(String re2AdviseTemplate) {
		this.re2AdviseTemplate = re2AdviseTemplate;
	}

	@Override
	public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
		String userText = chatClientRequest.prompt().getUserMessage().getText();
		String augmentedUserText = PromptTemplate.builder()
				.template(this.re2AdviseTemplate)
				.variables(Map.of("re2_input_query", (Object) userText))
				.build()
				.render();

		Prompt augmentedPrompt = chatClientRequest.prompt()
				.augmentUserMessage(msg -> new UserMessage(augmentedUserText));

		return chatClientRequest.mutate()
				.prompt(augmentedPrompt)
				.build();
	}

	@Override
	public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
		return chatClientResponse;
	}

	@Override
	public int getOrder() {
		return this.order;
	}

	public ReReadingAdvisor withOrder(int order) {
		this.order = order;
		return this;
	}

}
