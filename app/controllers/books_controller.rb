class BooksController < ApplicationController
  require 'socket'

  def book_params
    params.require(:book).permit(:isbn, :title, :author, :location)
  end

  def index
    public_ip = request.remote_ip
    local_ip = Socket.ip_address_list.detect{|intf| intf.ipv4_private?}
    user_ip = "#{public_ip}-#{local_ip.ip_address}"
    @sharedBooks = Book.where(:user_ip => user_ip)
    puts "sharedbooks: #{@sharedBooks}"
  end

  def new
    # render new page
    @book = Book.new
  end

  def create
    public_ip = request.remote_ip
    local_ip = Socket.ip_address_list.detect{|intf| intf.ipv4_private?}
    user_ip = "#{public_ip}-#{local_ip.ip_address}"
    @book = Book.new(book_params)
    @book.user_ip = user_ip
    if @book.save
      flash[:notice] = "#{@book.title} was successfully added."
      puts "#{@book.title} at #{@book.location} was successfully added to #{@book.user_ip}"
      redirect_to root_path
    else
      render 'new'
    end
  end

  def destroy
    redirect_to root_path
  end

  def search

  end

end