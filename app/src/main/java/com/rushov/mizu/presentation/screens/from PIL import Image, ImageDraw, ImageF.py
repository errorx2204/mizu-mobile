from PIL import Image, ImageDraw, ImageFont
import os

# Create output directory
output_dir = "/mnt/agents/output/mizu_logo"
os.makedirs(output_dir, exist_ok=True)

# Logo colors - water/finance theme
PRIMARY_COLOR = (0, 150, 200)  # Cyan blue
DARK_COLOR = (0, 100, 150)     # Darker blue
WHITE = (255, 255, 255)

def create_logo(size, bg_color=None):
    """Create MIZU logo with water drop shape and M letter"""
    img = Image.new('RGBA', (size, size), bg_color or (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    
    # Water drop shape
    padding = size // 8
    drop_width = size - (padding * 2)
    drop_height = size - (padding * 2)
    
    # Draw water drop (circle with pointed top)
    center_x = size // 2
    center_y = size // 2 + size // 16
    
    # Main circle body
    radius = drop_width // 2
    draw.ellipse(
        [center_x - radius, center_y - radius, 
         center_x + radius, center_y + radius],
        fill=PRIMARY_COLOR
    )
    
    # Pointed top (triangle)
    triangle_top = padding
    triangle_points = [
        (center_x, triangle_top),  # Top point
        (center_x - radius // 2, center_y - radius // 2),  # Left
        (center_x + radius // 2, center_y - radius // 2),  # Right
    ]
    draw.polygon(triangle_points, fill=PRIMARY_COLOR)
    
    # Highlight/shine on drop
    shine_radius = radius // 3
    shine_x = center_x - radius // 3
    shine_y = center_y - radius // 3
    draw.ellipse(
        [shine_x - shine_radius, shine_y - shine_radius,
         shine_x + shine_radius, shine_y + shine_radius],
        fill=(100, 200, 255, 180)
    )
    
    # Draw "M" letter in white
    try:
        font_size = size // 3
        font = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf", font_size)
    except:
        font = ImageFont.load_default()
    
    text = "M"
    bbox = draw.textbbox((0, 0), text, font=font)
    text_width = bbox[2] - bbox[0]
    text_height = bbox[3] - bbox[1]
    
    text_x = center_x - text_width // 2
    text_y = center_y - text_height // 2 - size // 32
    
    # Text shadow
    draw.text((text_x + 2, text_y + 2), text, fill=DARK_COLOR, font=font)
    # Main text
    draw.text((text_x, text_y), text, fill=WHITE, font=font)
    
    return img

# Generate logos for different sizes
sizes = {
    "play_store": 512,
    "xxxhdpi": 192,
    "xxhdpi": 144,
    "xhdpi": 96,
    "hdpi": 72,
    "mdpi": 48,
}

for name, size in sizes.items():
    logo = create_logo(size)
    logo.save(f"{output_dir}/mizu_logo_{name}_{size}.png", "PNG")
    print(f"Created {name}: {size}x{size}")

# Create adaptive icon background (solid color)
bg = Image.new('RGB', (512, 512), PRIMARY_COLOR)
bg.save(f"{output_dir}/mizu_background_512.png", "PNG")
print("Created adaptive background")

print(f"\nAll logos saved to: {output_dir}")