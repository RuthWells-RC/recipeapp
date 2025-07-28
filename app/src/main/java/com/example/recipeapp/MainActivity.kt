import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.*


data class Category(val id: Int, val name: String)
data class Ingredient(val name: String, val amount: String)
data class Recipe(
    val id: Int,
    val recipe: String,
    val category: Category,
    val ingredients: List<Ingredient>,
    val rating: Int
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { RecipeManagerApp() }
    }
}

@Composable
fun RecipeManagerApp() {
    val navController = rememberNavController()
    // Sample data
    val categories = remember {
        listOf(
            Category(1, "Dessert"),
            Category(2, "Main Course"),
            Category(3, "Appetizer")
        )
    }
    var recipes by remember { mutableStateOf(
        listOf(
            Recipe(
                1, "Chocolate Cake", categories[0],
                listOf(
                    Ingredient("Flour", "2 cups"),
                    Ingredient("Cocoa Powder", "1/2 cup"),
                ),
                5,
            ),
            Recipe(
                2, "Caesar Salad", categories[2],
                listOf(
                    Ingredient("Lettuce", "1 bunch"),
                    Ingredient("Croutons", "1 cup")
                ),
                4,
            )
        )
    )}

    NavHost(navController, startDestination = "list") {
        composable("list") {
            RecipeListScreen(
                recipes = recipes,
                onRecipeClick = { navController.navigate("detail/${it.id}") },
                onAddClick = { navController.navigate("add") }
            )
        }
        composable("detail/{recipeId}") { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getString("recipeId")?.toIntOrNull()
            val recipe = recipes.find { it.id == recipeId }
            recipe?.let {
                RecipeDetailScreen(recipe = it, onBack = { navController.popBackStack() })
            }
        }
        composable("add") {
            AddRecipeScreen(
                categories = categories,
                onAdd = { newRecipe ->
                    recipes = recipes + newRecipe.copy(id = (recipes.maxOfOrNull { it.id } ?: 0) + 1)
                    navController.popBackStack()
                },
                onCancel = { navController.popBackStack() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeListScreen(
    recipes: List<Recipe>,
    onRecipeClick: (Recipe) -> Unit,
    onAddClick: () -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Recipes") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Text("+")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(padding)
        ) {
            items(recipes) { recipe ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable { onRecipeClick(recipe) }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(recipe.name, style = MaterialTheme.typography.titleLarge)
                            Text("Category: ${recipe.category.name}", style = MaterialTheme.typography.bodyMedium)
                            Text("Rating: ${"★".repeat(recipe.rating)}", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(recipe: Recipe, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(recipe.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Text("<") }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(16.dp).padding(padding)) {
            Text("Category: ${recipe.category.name}", style = MaterialTheme.typography.titleMedium)
            Text("Rating: ${"★".repeat(recipe.rating)}", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(16.dp))
            Text("Ingredients:", style = MaterialTheme.typography.titleLarge)
            for (ingredient in recipe.ingredients) {
                Text("- ${ingredient.amount} ${ingredient.name}")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecipeScreen(
    categories: List<Category>,
    onAdd: (Recipe) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedCategoryIndex by remember { mutableStateOf(0) }
    var ingredients by remember { mutableStateOf(listOf<Ingredient>()) }
    var ingredientName by remember { mutableStateOf("") }
    var ingredientAmount by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf(3) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Recipe") },
                navigationIcon = {
                    IconButton(onClick = onCancel) { Text("<") }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(16.dp).padding(padding)) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Recipe Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            DropdownMenuBox(
                items = categories.map { it.name },
                selectedIndex = selectedCategoryIndex,
                onSelected = { selectedCategoryIndex = it },
                label = "Category"
            )
            Spacer(Modifier.height(8.dp))
            Text("Ingredients:")
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = ingredientAmount,
                    onValueChange = { ingredientAmount = it },
                    label = { Text("Amount") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(
                    value = ingredientName,
                    onValueChange = { ingredientName = it },
                    label = { Text("Name") },
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = {
                        if (ingredientName.isNotBlank() && ingredientAmount.isNotBlank()) {
                            ingredients = ingredients + Ingredient(ingredientName, ingredientAmount)
                            ingredientName = ""
                            ingredientAmount = ""
                        }
                    }, modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text("Add")
                }
            }
            for (ingredient in ingredients) {
                Text("- ${ingredient.amount} ${ingredient.name}")
            }
            Spacer(Modifier.height(8.dp))
            Text("Rating:")
            Row {
                for (i in 1..5) {
                    Text(
                        if (i <= rating) "★" else "☆",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier
                            .clickable { rating = i }
                            .padding(4.dp)
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Row {
                Button(
                    onClick = {
                        if (name.isNotBlank() && ingredients.isNotEmpty()) {
                            onAdd(
                                Recipe(
                                    id = 0, // real id is assigned in parent
                                    name = name,
                                    category = categories[selectedCategoryIndex],
                                    ingredients = ingredients,
                                    rating = rating
                                )
                            )
                        }
                    }
                ) { Text("Save") }
                Spacer(Modifier.width(8.dp))
                OutlinedButton(onClick = onCancel) { Text("Cancel") }
            }
        }
    }
}

@Composable
fun DropdownMenuBox(
    items: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    label: String
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedTextField(
            value = items[selectedIndex],
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEachIndexed { idx, item ->
                DropdownMenuItem(
                    text = { Text(item) },
                    onClick = {
                        onSelected(idx)
                        expanded = false
                    }
                )
            }
        }
    }
}
