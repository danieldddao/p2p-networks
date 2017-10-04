class BooksController < ApplicationController
  require 'socket'

  def book_params
    params.require(:book).permit(:isbn, :title, :author, :location)
  end

  def index
    puts "index page:"
    public_id = request.remote_ip
    local_ip = Socket.ip_address_list.detect{|intf| intf.ipv4_private?}
    puts public_id
    puts local_ip.ip_address
  end

  def new
    # render new page
    @book = Book.new
  end

  def create
    @book = Book.new(book_params)
    puts @book.title
    puts @book.isbn
    puts @book.author
    puts @book.location
      # if @book.save
    #   flash[:notice] = "#{@book.title} was successfully added."
    #   redirect_to root_path
    # else
    #   render 'new'
    # end
  end

  def search

  end

end