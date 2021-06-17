package com.codecool.poster.repository;

import com.codecool.poster.model.post.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    public List<Post> findAllByPersonIdIn(Collection<Long> person_id);

}
