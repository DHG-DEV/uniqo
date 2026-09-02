ᴍᴀᴠᴇʀɪxᴋ presents to you, UniQo.
UniQo is a student-focused Android platform designed to bring the college community together in one place.
It combines a student marketplace, roommate discovery, real-time communication, profiles, ratings, favorites, and other student-focused features into a single application.

## 🚀 What is UniQo?

UniQo is built to make student life easier by providing a dedicated platform where verified students can:

- Buy and sell products
- Discover products posted by other students
- Post items for sale
- Find rooms and roommates
- Connect and chat with other students
- Make offers on products
- Save favorite listings
- Manage their profile
- Rate products and sellers
- Discover student-related content in one place

## ✨ Key Features

### 🛍️ Student Marketplace
A dedicated marketplace where students can list products and discover items posted by other students.

### 📦 Product Listings
Users can create listings with:

- Product title
- Description
- Price
- Category
- Subcategory
- Condition
- Location
- Images

### ⭐ Rating System
UniQo includes a rating system designed around actual transactions.

Buyers can rate products they purchase and leave an optional review.

Seller ratings are calculated from the ratings received on products they have sold.

### 👤 Student Profiles
Profiles can display:

- Name
- Username
- Profile photo
- College
- Course
- Year
- Bio
- Location
- Verification status
- Rating
- Transactions
- Listed articles
- Member information

### 🏠 Roommate & Room Discovery
Students can discover available rooms and connect with potential roommates based on their preferences.

### 💬 Chat
Users can communicate with each other through in-app conversations.

Chat supports different message types including:

- Text
- Images
- Videos
- PDFs
- Files

### ❤️ Favorites
Users can save listings they are interested in and access them later.

### 🔎 Search & Filters
Users can discover products using search and filtering options such as category, condition, price, and location.

### 🔔 Notifications
Users can receive notifications related to their marketplace activity and interactions.

### 🔐 Student Verification
UniQo is designed around a student-focused verification system to help create a trusted campus community.

### ⚙️ Settings & Safety
The application includes sections for:

- Safety Center
- Community Guidelines
- Privacy Policy
- Terms & Conditions
- Help & Support

## 🛠️ Technology Stack

### Android
- Kotlin
- Jetpack Compose
- Material 3
- Android Studio

### Backend
- Supabase
- PostgreSQL
- Supabase Authentication
- Supabase Storage
- Supabase Edge Functions
- Row Level Security (RLS)

## 🗄️ Backend

UniQo uses Supabase as its backend infrastructure.

The backend handles:

- User authentication
- Student verification
- Profiles
- Marketplace listings
- Rooms
- Favorites
- Ratings
- Conversations
- Notifications
- File and image storage
- Database security
  
## 🏗️ Architecture

The application follows a repository-based architecture where the UI communicates with the repository layer, which handles backend operations through Supabase.

UI / Jetpack Compose
        ↓
UniqoRepository
        ↓
SupabaseRepository
        ↓
Supabase
        ↓
PostgreSQL / Storage / Edge Functions
