# This file is auto-generated from the current state of the database. Instead
# of editing this file, please use the migrations feature of Active Record to
# incrementally modify your database, and then regenerate this schema definition.
#
# Note that this schema.rb definition is the authoritative source for your
# database schema. If you need to create the application database on another
# system, you should be using db:schema:load, not running all the migrations
# from scratch. The latter is a flawed and unsustainable approach (the more migrations
# you'll amass, the slower it'll run and the greater likelihood for issues).
#
# It's strongly recommended that you check this file into your version control system.

ActiveRecord::Schema.define(version: 20171004144640) do

  create_table "books", force: :cascade do |t|
    t.string "user_client_ip"
    t.string "user_ip"
    t.string "port_number"
    t.string "title"
    t.string "isbn"
    t.string "author"
    t.string "location"
    t.boolean "isShared", default: true
    t.index ["user_client_ip", "user_ip", "title", "author", "location"], name: "user_client_ip_and_user_ip_and_title_and_author_and_location", unique: true
    t.index ["user_client_ip", "user_ip", "title", "isbn", "author"], name: "user_client_ip_and_user_ip_and_title_and_isbn_and_author", unique: true
  end

end
