class CreateBooks < ActiveRecord::Migration[5.1]
  def change
    create_table :books do |t|
      t.string :user_client_ip
      t.string :user_ip
      t.string :port_number
      t.string :title
      t.string :isbn
      t.string :author
      t.string :location
      t.boolean :isShared, :default => true
      t.index ["user_client_ip", "user_ip", "title", "isbn", "author"], name: "user_client_ip_and_user_ip_and_title_and_isbn_and_author", unique: true
      t.index ["user_client_ip", "user_ip", "title", "author", "location"], name: "user_client_ip_and_user_ip_and_title_and_author_and_location", unique: true
    end
  end
end
