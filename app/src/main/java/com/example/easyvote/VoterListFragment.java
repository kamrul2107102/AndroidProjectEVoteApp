package com.example.easyvote;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class VoterListFragment extends Fragment {

    private TextView textView;
    private Button button;
    private RequestQueue requestQueue;

    // This method fetches and parses JSON data from a given URL
    private void jsonParse() {
        String url = "https://jsonplaceholder.typicode.com/users";  // Example API URL

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            Log.d("API Response", response.toString());

                            textView.setText("");  // Clear any existing data in the TextView

                            for (int i = 0; i < response.length(); i++) {
                                JSONObject user = response.getJSONObject(i);
                                int id = user.getInt("id");
                                String name = user.getString("name");
                                String email = user.getString("email");

                                textView.append("ID: " + id + "\nName: " + name + "\nEmail: " + email + "\n\n");
                            }
                        } catch (JSONException e) {
                            textView.setText("Error parsing response.");
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Volley Error", error.toString());
                        textView.setText("Error: " + error.getMessage());
                    }
                });

        // Add the request to the request queue
        requestQueue.add(jsonArrayRequest);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the fragment's layout
        View rootView = inflater.inflate(R.layout.fragment_voter_list, container, false);  // Make sure this layout exists

        // Initialize the views
        textView = rootView.findViewById(R.id.textView);
        button = rootView.findViewById(R.id.button);
        requestQueue = Volley.newRequestQueue(getActivity());  // Use getActivity() to get the context for the Volley request queue

        // Set up button click listener to trigger data fetching
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jsonParse();  // Fetch data when the button is clicked
            }
        });

        return rootView;  // Return the inflated view
    }
}
