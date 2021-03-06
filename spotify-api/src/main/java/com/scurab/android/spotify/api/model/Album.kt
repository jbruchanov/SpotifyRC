/* 
Copyright (c) 2020 Kotlin Data Classes Generated from JSON powered by http://www.json2kotlin.com

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

For support, please feel free to contact me at https://www.linkedin.com/in/syedabsar */
package com.scurab.android.spotify.api.model
import com.google.gson.annotations.SerializedName

data class Album (

	@SerializedName("album_type") val album_type : String,
	@SerializedName("artists") val artists : List<Artists>,
	@SerializedName("available_markets") val available_markets : List<String>,
	@SerializedName("copyrights") val copyrights : List<Copyrights>,
	@SerializedName("external_ids") val external_ids : ExternalIds,
	@SerializedName("external_urls") val external_urls : ExternalUrls,
	@SerializedName("genres") val genres : List<String>,
	@SerializedName("href") val href : String,
	@SerializedName("id") val id : String,
	@SerializedName("images") val images : List<Image>,
	@SerializedName("label") val label : String,
	@SerializedName("name") val name : String,
	@SerializedName("popularity") val popularity : Int,
	@SerializedName("release_date") val release_date : String,
	@SerializedName("release_date_precision") val release_date_precision : String,
	@SerializedName("total_tracks") val total_tracks : Int,
	@SerializedName("tracks") val tracks : Tracks,
	@SerializedName("type") val type : String,
	@SerializedName("uri") val uri : String
) {
	fun getBestImageUrl(): String? = images.maxBy { it.height * it.width }?.url
}