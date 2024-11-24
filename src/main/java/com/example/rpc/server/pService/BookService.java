package com.example.rpc.server.pService;

import com.example.rpc.pojo.Book;

import java.util.List;

public interface BookService {


    Book getBook(Integer id);

    Book updateBook(Book book);



}
