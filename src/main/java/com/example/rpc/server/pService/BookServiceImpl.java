package com.example.rpc.server.pService;

import com.example.rpc.annotation.RpcService;
import com.example.rpc.pojo.Book;

import java.util.List;
import java.util.Map;

@RpcService(serviceName = "BookServiceImpl")
public class BookServiceImpl implements  BookService{


    @Override
    public Book getBook(Integer id) {
        return new Book(id,"大悲赋");
    }

    @Override
    public Book updateBook(Book book) {
        book.setName("修改后的:"+book.getName()+'s');
        return book;
    }



}
