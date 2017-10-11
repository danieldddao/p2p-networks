class CreateBooks < ActiveRecord::Migration[5.1]
  def change
    create_table :books do |t|
      t.string :user_ip
      t.string :port_number
      t.string :title
      t.string :isbn
      t.string :author
      t.string :location
      t.index ["user_ip", "title", "isbn", "author"], name: "user_ip_and_title_and_isbn_and_author", unique: true
      t.index ["user_ip", "title", "author", "location"], name: "user_ip_and_title_and_author_and_location", unique: true
    end

    # create_table :users do |t|
    #   t.string :user_ip
    #   t.boolean :is_online, default: false
    #   t.index ["user_ip"], name: "user_ip", unique: true
    # end
  end
end
