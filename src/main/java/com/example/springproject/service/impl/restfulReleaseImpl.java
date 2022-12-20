package com.example.springproject.service.impl;

import com.example.springproject.domain.Release;
import com.example.springproject.mapper.releaseRepository;
import com.example.springproject.service.restfulRelease;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class restfulReleaseImpl implements restfulRelease {
  @Autowired
  private releaseRepository releaseRepository;

  @Override
  public List<Release> findAll() {
    return releaseRepository.findAll();
  }

  @Override
  public long findCount() {
    return releaseRepository.count();
  }
}
