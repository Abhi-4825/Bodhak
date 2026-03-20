package com.example.bodhakfrontend.Backend.interfaces;

import java.nio.file.Path;

public interface Parser<T> {

   T parse(Path path);
   void invalidate(Path path);



}
