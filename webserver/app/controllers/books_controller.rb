class BooksController < ApplicationController

  # def book_params
  #   params.require(:book).permit(:user_ip, :isbn, :title, :author, :location)
  # end

  def index
    @book = Book.all
    puts @book
  end

  def new
    # render new page
  end

  def create
    @book = Book.new(:user_ip => params[:user_ip], :port_number => params[:port_number], :title => params[:title], :isbn => params[:isbn], :author => params[:author], :location => params[:location])
    if @book.save
      puts "#{@book.title} at #{@book.location} was successfully added to #{@book.user_ip}"
      render json: @resource, status: 201
      puts "added new book"
    else
      render json: @resource, status: 208
      puts "can't add new book"
    end
  end

  def destroy

  end

  def search
    books = Book.where(:title => params[:search_term])
    if !(books.blank?)
      puts "found books by title"
      puts books
      render json: books, status: 200
    else
        books = Book.where(:author => params[:search_term])
        if !(books.blank?)
        puts "found books by author"
        puts books
        render json: books, status: 200
        else
          books = Book.where(:isbn => params[:search_term])
          if !(books.blank?)
            puts "found books by isbn"
            puts books
            render json: books, status: 200
          else
            render json: "no book found", status: 204
          end
        end
      end
  end

end