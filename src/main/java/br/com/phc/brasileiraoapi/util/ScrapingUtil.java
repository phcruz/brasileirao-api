package br.com.phc.brasileiraoapi.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import br.com.phc.brasileiraoapi.dto.PartidaGoogleDTO;

@Service
public class ScrapingUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(ScrapingUtil.class);

	private static final String PARTIDA_NAO_INICIADA = "div[class=imso_mh__vs-at-sep imso_mh__team-names-have-regular-font]";
	private static final String JOGO_ROLANDO = "div[class=imso_mh__lv-m-stts-cont]";
	private static final String PARTIDA_ENCERRADA = "span[class=imso_mh__ft-mtch imso-medium-font imso_mh__ft-mtchc]";
	private static final String PLACAR_EQUIPE_CASA = "div[class=imso_mh__l-tm-sc imso_mh__scr-it imso-light-font]";
	private static final String PLACAR_EQUIPE_VISITANTE = "div[class=imso_mh__r-tm-sc imso_mh__scr-it imso-light-font]";
	private static final String LOGO_EQUIPE_CASA = "div[class=imso_mh__first-tn-ed imso_mh__tnal-cont imso-tnol]";
	private static final String LOGO_EQUIPE_VISITANTE = "div[class=imso_mh__second-tn-ed imso_mh__tnal-cont imso-tnol]";
	private static final String IMG_ITEM_LOGO = "img[class=imso_btl__mh-logo]";
	private static final String GOLS_EQUIPE_CASA = "div[class=imso_gs__tgs imso_gs__left-team]";
	private static final String GOLS_EQUIPE_VISITANTE = "div[class=imso_gs__tgs imso_gs__right-team]";
	private static final String DIV_ITEM_GOLS = "div[class=imso_gs__gs-r]";
	private static final String DIV_PENALIDADES = "div[class=imso_mh_s__psn-sc]";

	private static final String HTTPS = "https:";
	private static final String SRC = "src";
	private static final String SPAN = "span";
	private static final String PENALTIS = "Pênaltis";
	private static final String CASA = "CASA";
	private static final String VISITANTE = "VISITANTE";

	private static final String BASE_URL_GOOGLE = "https://www.google.com.br/search?q=";
	private static final String COMPLEMENTO_URL_GOOGLE = "&hl=pt-BR";

	public PartidaGoogleDTO obtemInformacoesGoogle(String url) {
		Document document = null;

		PartidaGoogleDTO partidaDTO = new PartidaGoogleDTO();

		try {
			LOGGER.info(url);
			// conecta no site
			document = Jsoup.connect(url).get();

			// recupera o titulo da pagina
			String title = document.title();
			LOGGER.info(title);

			StatusPartida statusPartida = obtemStatusPartida(document);
			partidaDTO.setStatusPartida(statusPartida);
			LOGGER.info("statusPartida: " + statusPartida);

			if (statusPartida != StatusPartida.PARTIDA_NAO_INICIADA) {
				String tempoPartida = obtemTempoPartida(document);
				LOGGER.info("tempoPartida: " + tempoPartida);
				partidaDTO.setTempoPartida(tempoPartida);

				Integer placarEquipeCasa = recuperaPlacarEquipe(document, PLACAR_EQUIPE_CASA);
				LOGGER.info("placarEquipeCasa: " + placarEquipeCasa);
				partidaDTO.setPlacarEquipeCasa(placarEquipeCasa);

				Integer placarEquipeVisitante = recuperaPlacarEquipe(document, PLACAR_EQUIPE_VISITANTE);
				LOGGER.info("placarEquipeVisitante: " + placarEquipeVisitante);
				partidaDTO.setPlacarEquipeVisitante(placarEquipeVisitante);

				String golsEquipeCasa = recuperaGolsEquipe(document, GOLS_EQUIPE_CASA);
				LOGGER.info("golsEquipeCasa: " + golsEquipeCasa);
				partidaDTO.setGolsEquipeCasa(golsEquipeCasa);

				String golsEquipeVisitante = recuperaGolsEquipe(document, GOLS_EQUIPE_VISITANTE);
				LOGGER.info("golsEquipeVisitante: " + golsEquipeVisitante);
				partidaDTO.setGolsEquipeVisitante(golsEquipeVisitante);
			}

			String nomeEquipeCasa = recuperaNomeEquipe(document, LOGO_EQUIPE_CASA);
			LOGGER.info("nomeEquipeCasa: " + nomeEquipeCasa);
			partidaDTO.setNomeEquipeCasa(nomeEquipeCasa);

			String nomeEquipeVisitante = recuperaNomeEquipe(document, LOGO_EQUIPE_VISITANTE);
			LOGGER.info("nomeEquipeVisitante: " + nomeEquipeVisitante);
			partidaDTO.setNomeEquipeVisitante(nomeEquipeVisitante);

			String urlLogoEquipeCasa = recuperaUrlLogoEquipe(document, LOGO_EQUIPE_CASA);
			LOGGER.info("urlLogoEquipeCasa: " + urlLogoEquipeCasa);
			partidaDTO.setUrlLogoEquipeCasa(urlLogoEquipeCasa);

			String urlLogoEquipeVisitante = recuperaUrlLogoEquipe(document, LOGO_EQUIPE_VISITANTE);
			LOGGER.info("urlLogoEquipeVisitante: " + urlLogoEquipeVisitante);
			partidaDTO.setUrlLogoEquipeVisitante(urlLogoEquipeVisitante);

			Integer placarEstendidoEquipeCasa = buscaPenalidades(document, CASA);
			LOGGER.info("placarEstendidoEquipeCasa: " + placarEstendidoEquipeCasa);
			partidaDTO.setPlacarEstendidoEquipeCasa(placarEstendidoEquipeCasa);

			Integer placarEstendidoEquipeVisitante = buscaPenalidades(document, VISITANTE);
			LOGGER.info("placarEstendidoEquipeVisitante: " + placarEstendidoEquipeVisitante);
			partidaDTO.setPlacarEstendidoEquipeVisitante(placarEstendidoEquipeVisitante);

		} catch (IOException e) {
			LOGGER.error(e.getMessage());
			return null;
		}

		return partidaDTO;
	}

	public StatusPartida obtemStatusPartida(Document document) {
		StatusPartida statusPartida = StatusPartida.PARTIDA_NAO_INICIADA;
		// SITUACOES
		// 1 - Consulta antes do inicio partida
		boolean isTempoPartida = document.select(PARTIDA_NAO_INICIADA).isEmpty();
		if (!isTempoPartida) {
			statusPartida = StatusPartida.PARTIDA_NAO_INICIADA;
		}

		// 2 - jogo rolando ou intervalo
		isTempoPartida = document.select(JOGO_ROLANDO).isEmpty();
		if (!isTempoPartida) {
			String tempoPartida = document.select(JOGO_ROLANDO).first().text();
			statusPartida = StatusPartida.PARTIDA_EM_ANDAMENTO;
			if (tempoPartida.contains(PENALTIS)) {
				statusPartida = StatusPartida.PARTIDA_PENALTIS;
			}
		}

		// 3 - jogo encerrado
		isTempoPartida = document.select(PARTIDA_ENCERRADA).isEmpty();
		if (!isTempoPartida) {
			statusPartida = StatusPartida.PARTIDA_ENCERRADA;
		}

		return statusPartida;
	}

	public String obtemTempoPartida(Document document) {
		// situações
		// 1 - Consulta antes do inicio partida
		String tempoPartida = null;
		boolean isTempoPartida = document.select(PARTIDA_NAO_INICIADA).isEmpty();
		if (!isTempoPartida) {
			tempoPartida = document.select(PARTIDA_NAO_INICIADA).first().text();
		}

		// 2 - jogo rolando ou intervalo
		isTempoPartida = document.select(JOGO_ROLANDO).isEmpty();
		if (!isTempoPartida) {
			tempoPartida = document.select(JOGO_ROLANDO).first().text();
		}

		// 3 - jogo encerrado
		isTempoPartida = document.select(PARTIDA_ENCERRADA).isEmpty();
		if (!isTempoPartida) {
			tempoPartida = document.select(PARTIDA_ENCERRADA).first().text();
		}

		return corrigeTempoPartida(tempoPartida);
	}

	private static String corrigeTempoPartida(String tempo) {
		String tempoPartida = "";
		if (tempo.contains("'")) {
			tempoPartida = tempo.replace(" ", "");
			tempoPartida = tempoPartida.replace("'", "").concat(" min");
		} else {
			if (tempo.contains("+")) {
				tempoPartida = tempo.replace(" ", "").concat(" min");
			} else {
				return tempo;
			}
		}
		return tempoPartida;
	}

	public Integer recuperaPlacarEquipe(Document document, String itemHtml) {
		String placarEquipe = document.select(itemHtml).first().text();
		return formataPlacarStringInteger(placarEquipe);
	}

	public Integer formataPlacarStringInteger(String placar) {
		Integer valor;
		try {
			valor = Integer.parseInt(placar);
		} catch (Exception e) {
			valor = 0;
		}

		return valor;
	}

	public String recuperaUrlLogoEquipe(Document document, String itemHtml) {
		Element elementLogoEquipe = document.select(itemHtml).first();
		String urlLogo = HTTPS + elementLogoEquipe.select(IMG_ITEM_LOGO).attr(SRC);

		return urlLogo;
	}

	public String recuperaNomeEquipe(Document document, String itemHtml) {
		Element elementNomeEquipe = document.select(itemHtml).first();
		String nomeEquipe = elementNomeEquipe.select(SPAN).text();

		return nomeEquipe;
	}

	public String recuperaGolsEquipe(Document document, String itemHtml) {
		List<String> golsEquipe = new ArrayList<>();

		Elements timeCasa = document.select(itemHtml).select(DIV_ITEM_GOLS);
		for (Element e : timeCasa) {
			String infoGol = e.select(DIV_ITEM_GOLS).text();
			golsEquipe.add(infoGol);
		}

		return golsEquipe.isEmpty() ? null : String.join(", ", golsEquipe);
	}

	public Integer buscaPenalidades(Document document, String tipoEquipe) {
		boolean isPenalidades = document.select(DIV_PENALIDADES).isEmpty();
		if (!isPenalidades) {
			String penalidades = document.select(DIV_PENALIDADES).text();
			String completo = penalidades.substring(0, 5).replace(" ", "");
			String[] divisao = completo.split("-");

			return tipoEquipe.equals(CASA) ? formataPlacarStringInteger(divisao[0])
					: formataPlacarStringInteger(divisao[1]);
		}
		return null;
	}

	public String montaUrlGoogle(String nomeEquipeCasa, String nomeEquipeVisitante) {
		try {
			String equipeCasa = nomeEquipeCasa.replace(" ", "+").replace("-", "+");
			String equipeVisitante = nomeEquipeVisitante.replace(" ", "+").replace("-", "+");

			return BASE_URL_GOOGLE + equipeCasa + "+x+" + equipeVisitante + COMPLEMENTO_URL_GOOGLE;
		} catch (Exception e) {
			LOGGER.error("ERRO: {}", e.getMessage());
		}
		return null;
	}

}
