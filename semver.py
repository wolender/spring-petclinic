import sys

def calculate_next_version(latest_version):
    # Split the version string into major, minor, and patch parts
    major, minor, patch = map(int, latest_version.split('.'))


    minor += 1

    next_version = f"{major}.{minor}.{patch}"


    print(next_version)

if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("Usage: python script.py <latest_version>")
        sys.exit(1)
    latest_version = sys.argv[1]
    calculate_next_version(latest_version)
