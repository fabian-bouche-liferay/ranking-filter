package com.liferay.samples.fbo.search.tuning.rankings;

import com.liferay.portal.search.tuning.rankings.web.internal.index.Ranking;
import com.liferay.portal.search.tuning.rankings.web.internal.index.RankingFields;
import com.liferay.portal.search.tuning.rankings.web.internal.index.DocumentToRankingTranslator;

import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.document.Document;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.osgi.service.component.annotations.Component;

@Component(service = DocumentToRankingTranslator.class)
public class CustomDocumentToRankingTranslatorImpl implements DocumentToRankingTranslator {

	@Override
	public Ranking translate(Document document, String rankingDocumentId) {
		return builder(
		).aliases(
			_getAliases(document)
		).hiddenDocumentIds(
			document.getStrings(RankingFields.BLOCKS)
		).inactive(
			document.getBoolean(RankingFields.INACTIVE)
		).indexName(
			document.getString("index")
		).name(
			_getName(document)
		).pins(
			_getPins(document)
		).queryString(
			_getQueryString(document)
		).rankingDocumentId(
			rankingDocumentId
		).build();
	}

	protected Ranking.RankingBuilder builder() {
		return new Ranking.RankingBuilder();
	}

	private List<String> _getAliases(Document document) {
		List<String> aliases = document.getStrings(RankingFields.ALIASES);

		if (ListUtil.isEmpty(aliases)) {
			List<String> queryStrings = document.getStrings(
				RankingFields.QUERY_STRINGS);

			queryStrings.remove(document.getString(RankingFields.QUERY_STRING));

			return queryStrings;
		}

		return aliases;
	}

	private String _getName(Document document) {
		String string = document.getString(RankingFields.NAME);

		if (Validator.isBlank(string)) {
			return _getQueryString(document);
		}

		return string;
	}

	private List<Ranking.Pin> _getPins(Document document) {
		List<?> values = document.getValues(RankingFields.PINS);

		if (ListUtil.isEmpty(values)) {
			return Collections.emptyList();
		}

		List<Map<String, String>> maps = (List<Map<String, String>>)values;

		Stream<Map<String, String>> stream = maps.stream().filter(this::_isCurrentSite);

		Stream<Ranking.Pin> pinStream = stream.map(this::_toPin);

		return pinStream.collect(Collectors.toList());
	}

	private String _getQueryString(Document document) {
		String string = document.getString(RankingFields.QUERY_STRING);

		if (Validator.isBlank(string)) {
			List<String> strings = _getAliases(document);

			if (ListUtil.isNotEmpty(strings)) {
				return strings.get(0);
			}
		}

		return string;
	}

	private Ranking.Pin _toPin(Map<String, String> map) {
		return new Ranking.Pin(
			GetterUtil.getInteger(map.get("position")), map.get("uid"));
	}
	
	private boolean _isCurrentSite(Map<String,String> map) {
		System.out.println(map.get("uid"));		
		// Do your filering logic here and return false if you don't want the result
		return true;
	}

}