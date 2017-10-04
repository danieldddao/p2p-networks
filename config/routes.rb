Rails.application.routes.draw do
  get 'books/index'
  root 'books#index'
  post 'books/search', to: 'books#search'
end
