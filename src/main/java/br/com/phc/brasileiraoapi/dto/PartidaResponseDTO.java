package br.com.phc.brasileiraoapi.dto;

import java.io.Serializable;
import java.util.List;

import br.com.phc.brasileiraoapi.entity.Partida;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PartidaResponseDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<Partida> partidas;
}
