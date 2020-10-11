package br.com.phc.brasileiraoapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.phc.brasileiraoapi.entity.Partida;

@Repository
public interface PartidaRepository extends JpaRepository<Partida, Long>{

}
