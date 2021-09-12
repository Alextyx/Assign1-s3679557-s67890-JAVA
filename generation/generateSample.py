import numpy as np
import pandas as pd


def gen_float(low, high, n_rows, n_cols=None):
    """Generate dataset randomly.
    Arguments:
        low {int} -- The minimum value of element generated.
        high {int} -- The maximum value of element generated.
        n_rows {int} -- Number of rows.
        n_cols {int} -- Number of columns.
    Returns:
        list -- 1d or 2d list with int
    """
    if n_cols is None:
        ret = [np.random.uniform(low, high) for _ in range(n_rows)]
    else:
        ret = [[np.random.uniform(low, high) for _ in range(n_cols)]
               for _ in range(n_rows)]
    return ret


def gen_int(low, high, n_rows, n_cols=None):
    """Generate dataset randomly.
    Arguments:
        low {int} -- The minimum value of element generated.
        high {int} -- The maximum value of element generated.
        n_rows {int} -- Number of rows.
        n_cols {int} -- Number of columns.
    Returns:
        list -- 1d or 2d list with int
    """
    if n_cols is None:
        ret = [np.random.random(low, high) for _ in range(n_rows)]
    else:
        ret = [[np.random.random(low, high) for _ in range(n_cols)]
               for _ in range(n_rows)]
    return ret


def gen_cat(n_rows, cat=["restaurant", "education", "hospital"]):
    RANDOM = np.random.choice(cat)
    ret = [np.random.choice(cat) for _ in range(n_rows)]
    return ret


def generate_fix_number_sample_list(N: int):
    SEED = 2021
    np.random.seed(SEED)
    lat_list = gen_float(-90, 90, N)
    lon_list = gen_float(-180, 180, N)
    id_list = ["id" + str(i) for i in range(N)]
    cat_list = gen_cat(N)
    d = {"id": id_list, "cat": cat_list, "lat": lat_list, "lon": lon_list}
    df = pd.DataFrame(data=d)
    filename = "sample_data_" + str(N)
    df.to_csv(path_or_buf=filename, sep=" ", header=False, index=False)
    return df


def generate_command(df, type: str, K: int, M: int):
    N = len(df)
    df = df.sample(n=M)
    df['k-number'] = K
    df['command'] = type
    df_out = df.drop(columns=['id'])

    # Get the DataFrame column names as a list
    clist = list(df_out.columns)

    # Rearrange list the way you like
    clist_new = clist[-1:] + clist[:-1]  # brings the last column in the first place
    # Pass the new list to the DataFrame - like a key list in a dict
    df_out = df_out[clist_new]

    filename = "test_{}_k{}_M{}.in".format(N, K, M)
    df_out.to_csv(path_or_buf=filename, sep=" ", header=False, index=False)


def generate_sample(N: int, K: int, M: int):
    df = generate_fix_number_sample_list(N)
    generate_command(df, "S", K, M)


if __name__ == '__main__':
    generate_sample(1000, 1, 10)
    generate_sample(1000, 3, 10)
    generate_sample(1000, 7, 10)
