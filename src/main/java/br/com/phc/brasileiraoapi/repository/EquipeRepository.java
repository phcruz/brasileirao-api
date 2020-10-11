package br.com.phc.brasileiraoapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.phc.brasileiraoapi.entity.Equipe;

@Repository
public interface EquipeRepository extends JpaRepository<Equipe, Long>{

}
