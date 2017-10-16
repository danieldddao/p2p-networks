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
    user_client_ip = request.remote_ip
    puts "client user ip: " + user_client_ip
    @book = Book.new(:user_client_ip => user_client_ip, :user_ip => params[:user_ip], :port_number => params[:port_number], :title => params[:title], :isbn => params[:isbn], :author => params[:author], :location => params[:location])
    if @book.save
      puts "#{@book.title} at #{@book.location} was successfully added to #{@book.user_ip}"
      render json: @resource, status: 201
      puts "added new book"
    else
      render json: @resource, status: 208
      puts "can't add new book"
    end
  end


  def getAllMyBooks
    user_client_ip = request.remote_ip
    books = Book.where(:user_client_ip => user_client_ip)
    if !(books.blank?)
      puts "All my books:"
      puts books
      render json: books, status: 200
    else
      render json: "no book found", status: 204
    end
  end


  def unshareBook
    user_client_ip = request.remote_ip
    puts "delete book: " + user_client_ip + ", " + params[:user_ip] + ": " + params[:port_number]
    book = Book.where(:user_client_ip => user_client_ip, user_ip: params[:user_ip], port_number: params[:port_number])
    book.isShared = false
    book.save
  end


  def updateLocation
    user_client_ip = request.remote_ip
    book = Book.where(:user_client_ip => user_client_ip, :user_ip => params[:user_ip], :port_number => params[:port_number], :title => params[:title], :author => params[:author])

    if book.blank?
      render json: book, status: 204
    else
      book.location = params[:location]
      book.isShare = true
      if book.save
        render json: book, status: 200
      else
        render json: book, status: 204
      end
    end
  end


  def updateSharingStatus
    user_client_ip = request.remote_ip
    book = Book.where(:user_client_ip => user_client_ip, :user_ip => params[:user_ip], :port_number => params[:port_number], :title => params[:title], :author => params[:author])

    if book.blank?
      render json: book, status: 204
    else
      if params[:sharing_status] == "true"
        book.isShare = true
      else
        book.isShare = false
      end
      if book.save
        render json: book, status: 200
      else
        render json: book, status: 204
      end
    end
  end


  def search
    if params[:search_term] == ""
      books = Book.all
      render json: books, status: 200
    else
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

end