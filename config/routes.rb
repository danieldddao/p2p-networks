Rails.application.routes.draw do
  root 'books#index'
  get 'books/index'
  get 'books/new'
  post 'books/new', to: 'books#create'
  post 'books/search', to: 'books#search'
  delete 'book', to: 'books#destroy'
end
