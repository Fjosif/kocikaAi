using Microsoft.AspNetCore.Mvc;
using Mscc.GenerativeAI;

var builder = WebApplication.CreateBuilder(args);

// Ensure the app listens on the port assigned by the hosting provider (e.g. Render/Railway)
var port = Environment.GetEnvironmentVariable("PORT") ?? "5000";
builder.WebHost.UseUrls($"http://0.0.0.0:{port}");

// Configure Gemini
var apiKey = builder.Configuration["Gemini:ApiKey"];
builder.Services.AddSingleton(new GoogleAI(apiKey));

var app = builder.Build();

app.MapGet("/", () => "AI Kočička Backend (Powered by Gemini)");

app.MapPost("/cat/respond", async ([FromBody] CatStatusRequest request, GoogleAI googleAI) => {
    try {
        // Use string for model name to be safe
        var model = googleAI.GenerativeModel("gemini-1.5-flash"); 
        
        var prompt = $@"Jsi milá a hravá kočička. Mluvíš k malému dítěti česky. 
Dítě právě udělalo: {request.LastAction}.
Tvůj aktuální stav: Hlad={request.Hunger}/100, Energie={request.Energy}/100, Nálada={request.Mood}/100.
Reaguj krátce (max 2 věty). Buď veselá, milá a dětsky bezpečná. Odpovídej v první osobě jako kočka.";

        var response = await model.GenerateContent(prompt);
        var text = response.Text ?? "Mňau! Máš mě rád?";
        
        return Results.Ok(new CatResponse { Message = text.Trim() });
    }
    catch (Exception) {
        return Results.Ok(new CatResponse { Message = "Mňau! (Něco se pokazilo, ale pořád tě mám ráda!)" });
    }
});

app.MapPost("/story/generate", async ([FromBody] StoryRequest request, GoogleAI googleAI) => {
    try {
        var model = googleAI.GenerativeModel("gemini-1.5-flash");
        
        var prompt = $@"Vypravuj krátkou (cca 100-150 slov) a milou pohádku pro malé dítě česky. 
Pohádka je o kočičce. Nálada kočky je: {request.Mood}/100. 
Pokud je nálada vysoká, pohádka je veselá. Pokud nižší, kočička v pohádce hledá útěchu nebo kamaráda.
Styl: {request.Style}. Buď laskavý a používej jednoduchý jazyk.";

        var response = await model.GenerateContent(prompt);
        var text = response.Text ?? "Byla jednou jedna kočička...";
        
        return Results.Ok(new StoryResponse { StoryText = text.Trim() });
    }
    catch (Exception) {
        return Results.Ok(new StoryResponse { StoryText = "Byla jednou jedna kočička, která tě měla moc ráda. A to je konec pohádky." });
    }
});

app.Run();

public record CatStatusRequest(int Hunger, int Energy, int Hygiene, int Mood, int Health, string LastAction);
public record CatResponse { public string Message { get; init; } = ""; }

public record StoryRequest(int Mood, string Style);
public record StoryResponse { public string StoryText { get; init; } = ""; }
