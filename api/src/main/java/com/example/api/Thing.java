package com.example.api;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import lombok.Data;

@Data
@Entity
public class Thing {
  @Id
  @GeneratedValue
  long id;
  String name;
}
